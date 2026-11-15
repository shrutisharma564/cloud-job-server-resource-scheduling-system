package com.cloudscheduler.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/resources")
public class ResourceSafetyController {
    public record SafetyRequest(List<String> processNames,List<Integer> available,List<List<Integer>> allocation,List<List<Integer>> maximum) {}
    public record DeadlockRequest(List<String> processNames,List<Integer> available,List<List<Integer>> allocation,List<List<Integer>> request) {}

    @PostMapping("/bankers-check")
    public Map<String,Object> bankers(@RequestBody SafetyRequest r){
        validate(r.processNames(),r.available(),r.allocation(),r.maximum());int n=r.processNames().size(),m=r.available().size();int[] work=r.available().stream().mapToInt(Integer::intValue).toArray();boolean[] finish=new boolean[n];List<String> safe=new ArrayList<>();List<List<Integer>> need=new ArrayList<>();
        for(int i=0;i<n;i++){List<Integer> row=new ArrayList<>();for(int j=0;j<m;j++){int v=r.maximum().get(i).get(j)-r.allocation().get(i).get(j);if(v<0)throw new IllegalArgumentException("Maximum claim cannot be less than current allocation");row.add(v);}need.add(row);}
        boolean progress=true;while(progress){progress=false;for(int i=0;i<n;i++){if(finish[i])continue;boolean can=true;for(int j=0;j<m;j++)if(need.get(i).get(j)>work[j]){can=false;break;}if(can){for(int j=0;j<m;j++)work[j]+=r.allocation().get(i).get(j);finish[i]=true;safe.add(r.processNames().get(i));progress=true;}}}
        List<String> blocked=new ArrayList<>();for(int i=0;i<n;i++)if(!finish[i])blocked.add(r.processNames().get(i));Map<String,Object> out=new LinkedHashMap<>();out.put("safeState",blocked.isEmpty());out.put("safeSequence",safe);out.put("unfinishedProcesses",blocked);out.put("needMatrix",need);out.put("message",blocked.isEmpty()?"System is in a safe state.":"Unsafe state: no complete safe sequence exists.");return out;
    }

    @PostMapping("/deadlock-check")
    public Map<String,Object> deadlock(@RequestBody DeadlockRequest r){
        validate(r.processNames(),r.available(),r.allocation(),r.request());int n=r.processNames().size(),m=r.available().size();int[] work=r.available().stream().mapToInt(Integer::intValue).toArray();boolean[] finish=new boolean[n];
        for(int i=0;i<n;i++){boolean zero=true;for(int j=0;j<m;j++)if(r.allocation().get(i).get(j)!=0){zero=false;break;}finish[i]=zero;}
        boolean progress=true;while(progress){progress=false;for(int i=0;i<n;i++){if(finish[i])continue;boolean can=true;for(int j=0;j<m;j++)if(r.request().get(i).get(j)>work[j]){can=false;break;}if(can){for(int j=0;j<m;j++)work[j]+=r.allocation().get(i).get(j);finish[i]=true;progress=true;}}}
        List<String> deadlocked=new ArrayList<>();for(int i=0;i<n;i++)if(!finish[i])deadlocked.add(r.processNames().get(i));Map<String,Object> out=new LinkedHashMap<>();out.put("deadlockDetected",!deadlocked.isEmpty());out.put("deadlockedProcesses",deadlocked);out.put("message",deadlocked.isEmpty()?"No deadlock detected.":"Potential deadlock detected among listed processes.");return out;
    }
    private void validate(List<String> names,List<Integer> available,List<List<Integer>> allocation,List<List<Integer>> other){
        if(names==null||names.isEmpty()||available==null||available.isEmpty()||allocation==null||other==null||allocation.size()!=names.size()||other.size()!=names.size())throw new IllegalArgumentException("Provide process names, available resources, and matching matrices.");int m=available.size();for(Integer v:available)if(v==null||v<0)throw new IllegalArgumentException("Resource counts cannot be negative.");for(int i=0;i<names.size();i++){if(allocation.get(i)==null||other.get(i)==null||allocation.get(i).size()!=m||other.get(i).size()!=m)throw new IllegalArgumentException("Every matrix row must match the available resource count.");for(Integer v:allocation.get(i))if(v==null||v<0)throw new IllegalArgumentException("Matrix values cannot be negative.");for(Integer v:other.get(i))if(v==null||v<0)throw new IllegalArgumentException("Matrix values cannot be negative.");}}
}
