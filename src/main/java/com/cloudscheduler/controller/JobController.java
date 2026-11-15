package com.cloudscheduler.controller;

import com.cloudscheduler.model.Job;
import com.cloudscheduler.repository.JobRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController @RequestMapping("/api/jobs")
public class JobController {
    private final JobRepository jobs;
    public JobController(JobRepository jobs){this.jobs=jobs;}
    @GetMapping public List<Job> all(){return jobs.findAll();}
    @GetMapping("/{id}") public Job one(@PathVariable Long id){return jobs.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Job not found"));}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Job create(@Valid @RequestBody JobRequest r){Job job=new Job(r.name(),r.cpuRequired(),r.ramRequiredGb(),r.storageRequiredGb(),r.priority());job.setBurstTime(r.burstTime()==null?1:r.burstTime());return jobs.save(job);}
    @PostMapping("/{id}/queue") public Job queue(@PathVariable Long id){Job j=one(id);if("ALLOCATED".equals(j.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Allocated job cannot be re-queued");j.setStatus("QUEUED");return jobs.save(j);}
    public record JobRequest(@NotBlank String name,@Min(1) int cpuRequired,@Min(1) int ramRequiredGb,@Min(1) int storageRequiredGb,@Min(1) int priority,@Min(1) Integer burstTime){}
}
