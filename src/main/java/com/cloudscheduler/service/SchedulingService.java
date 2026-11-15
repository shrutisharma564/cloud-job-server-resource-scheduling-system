package com.cloudscheduler.service;

import com.cloudscheduler.dto.AllocationView;
import com.cloudscheduler.model.*;
import com.cloudscheduler.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchedulingService {
    private final JobRepository jobs; private final ServerRepository servers; private final AllocationRepository allocations;
    private final SchedulingRunRepository runs; private final ResourceLogRepository resourceLogs;
    public SchedulingService(JobRepository jobs, ServerRepository servers, AllocationRepository allocations, SchedulingRunRepository runs, ResourceLogRepository resourceLogs){this.jobs=jobs;this.servers=servers;this.allocations=allocations;this.runs=runs;this.resourceLogs=resourceLogs;}

    @Transactional
    public Map<String,Object> run(String algorithm,int quantum) {
        String mode=algorithm==null?"FCFS":algorithm.trim().toUpperCase(Locale.ROOT);
        if(!Set.of("FCFS","PRIORITY","SJF","ROUND_ROBIN").contains(mode)) throw new IllegalArgumentException("Supported algorithms: FCFS, PRIORITY, SJF, ROUND_ROBIN");
        if("ROUND_ROBIN".equals(mode) && quantum<1) throw new IllegalArgumentException("Time quantum must be at least 1");
        List<Job> queue=new ArrayList<>(jobs.findByStatus("QUEUED"));
        queue.sort(Comparator.comparing(Job::getCreatedAt).thenComparing(Job::getId));
        List<Map<String,Object>> gantt= new ArrayList<>(); List<Map<String,Object>> jobMetrics=new ArrayList<>();
        Map<Long,Integer> firstStart=new HashMap<>(), completion=new HashMap<>(); int clock=0;
        if("ROUND_ROBIN".equals(mode)) {
            Map<Long,Integer> remaining=new LinkedHashMap<>(); for(Job j:queue) remaining.put(j.getId(),Math.max(1,j.getBurstTime()));
            ArrayDeque<Job> ready=new ArrayDeque<>(queue);
            while(!ready.isEmpty()) { Job j=ready.removeFirst(); int start=clock; firstStart.putIfAbsent(j.getId(),start); int slice=Math.min(quantum,remaining.get(j.getId())); clock+=slice; remaining.put(j.getId(),remaining.get(j.getId())-slice); gantt.add(segment(j,start,clock)); if(remaining.get(j.getId())>0) ready.addLast(j); else completion.put(j.getId(),clock); }
        } else {
            List<Job> ordered=new ArrayList<>(queue);
            if("PRIORITY".equals(mode)) ordered.sort(Comparator.comparingInt(Job::getPriority).thenComparing(Job::getCreatedAt));
            if("SJF".equals(mode)) ordered.sort(Comparator.comparingInt(Job::getBurstTime).thenComparing(Job::getCreatedAt));
            for(Job j:ordered){int start=clock;firstStart.put(j.getId(),start);clock+=Math.max(1,j.getBurstTime());completion.put(j.getId(),clock);gantt.add(segment(j,start,clock));}
        }
        List<Job> allocationOrder=new ArrayList<>(); Set<Long> seen=new HashSet<>();
        for(Map<String,Object> seg:gantt){Long id=(Long)seg.get("jobId");if(seen.add(id))queue.stream().filter(j->j.getId().equals(id)).findFirst().ifPresent(allocationOrder::add);}
        List<AllocationView> created=new ArrayList<>(); List<Long> waiting=new ArrayList<>(); List<Server> serverList=servers.findAll();
        for(Job job:allocationOrder){Optional<Server> candidate=serverList.stream().filter(s->s.canFit(job)).sorted(Comparator.comparingInt(Server::getAvailableCpu).thenComparingInt(Server::getAvailableRamGb)).findFirst();
            if(candidate.isEmpty()){waiting.add(job.getId());continue;} Server server=candidate.get();server.allocate(job);job.setStatus("ALLOCATED");Allocation a=allocations.save(new Allocation(job,server));resourceLogs.save(new ResourceLog(server,"ALLOCATE_JOB_"+job.getId()));created.add(toView(a));}
        double sumWait=0,sumTurn=0,sumResponse=0;
        for(Job j:queue){int finish=completion.getOrDefault(j.getId(),0),start=firstStart.getOrDefault(j.getId(),0),burst=Math.max(1,j.getBurstTime());int wait=Math.max(0,finish-burst);int turn=finish;int response=start;sumWait+=wait;sumTurn+=turn;sumResponse+=response;Map<String,Object> m=new LinkedHashMap<>();m.put("jobId",j.getId());m.put("jobName",j.getName());m.put("burstTime",burst);m.put("waitingTime",wait);m.put("turnaroundTime",turn);m.put("responseTime",response);m.put("completionTime",finish);jobMetrics.add(m);}
        int count=queue.size();double aw=count==0?0:sumWait/count,at=count==0?0:sumTurn/count,ar=count==0?0:sumResponse/count;
        runs.save(new SchedulingRun(mode,"ROUND_ROBIN".equals(mode)?quantum:0,count,created.size(),waiting.size(),aw,at,ar));
        Map<String,Object> result=new LinkedHashMap<>();result.put("algorithm",mode);result.put("timeQuantum","ROUND_ROBIN".equals(mode)?quantum:null);result.put("allocatedCount",created.size());result.put("waitingJobIds",waiting);result.put("allocations",created);result.put("ganttChart",gantt);result.put("jobMetrics",jobMetrics);result.put("averageWaitingTime",round(aw));result.put("averageTurnaroundTime",round(at));result.put("averageResponseTime",round(ar));result.put("throughput",clock==0?0:round((double)count/clock));result.put("totalSimulationTime",clock);return result;
    }
    private Map<String,Object> segment(Job j,int start,int end){Map<String,Object> m=new LinkedHashMap<>();m.put("jobId",j.getId());m.put("jobName",j.getName());m.put("start",start);m.put("end",end);return m;}
    private double round(double v){return Math.round(v*100.0)/100.0;}
    @Transactional(readOnly=true) public List<AllocationView> list(){return allocations.findAll().stream().map(this::toView).collect(Collectors.toList());}
    @Transactional(readOnly=true) public List<SchedulingRun> history(){return runs.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,"createdAt"));}
    @Transactional(readOnly=true) public List<ResourceLog> resourceHistory(){return resourceLogs.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,"createdAt"));}
    @Transactional public AllocationView complete(Long jobId){Allocation a=allocations.findFirstByJob_IdAndStatus(jobId,"ACTIVE").orElseThrow(()->new NoSuchElementException("No active allocation for job "+jobId));a.getServer().release(a.getJob());a.getJob().setStatus("COMPLETED");a.setStatus("COMPLETED");a.setCompletedAt(LocalDateTime.now());resourceLogs.save(new ResourceLog(a.getServer(),"RELEASE_JOB_"+jobId));return toView(a);}
    private AllocationView toView(Allocation a){return new AllocationView(a.getId(),a.getJob().getId(),a.getJob().getName(),a.getServer().getId(),a.getServer().getName(),a.getAllocatedAt(),a.getCompletedAt(),a.getStatus());}
}
