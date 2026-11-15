package com.cloudscheduler.controller;

import com.cloudscheduler.dto.AllocationView;
import com.cloudscheduler.model.ResourceLog;
import com.cloudscheduler.model.SchedulingRun;
import com.cloudscheduler.service.SchedulingService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api")
public class SchedulingController {
    private final SchedulingService scheduler;
    public SchedulingController(SchedulingService scheduler){this.scheduler=scheduler;}
    @PostMapping("/scheduling/run") public Map<String,Object> run(@RequestParam(defaultValue="FCFS") String algorithm,@RequestParam(defaultValue="2") int quantum){return scheduler.run(algorithm,quantum);}
    @GetMapping("/allocations") public List<AllocationView> allocations(){return scheduler.list();}
    @PostMapping("/jobs/{jobId}/complete") public AllocationView complete(@PathVariable Long jobId){return scheduler.complete(jobId);}
    @GetMapping("/scheduling/history") public List<SchedulingRun> history(){return scheduler.history();}
    @GetMapping("/resources/history") public List<ResourceLog> resourceHistory(){return scheduler.resourceHistory();}
}
