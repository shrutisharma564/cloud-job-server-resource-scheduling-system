package com.cloudscheduler.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs", indexes = {@Index(name="idx_jobs_status_created", columnList="status, created_at"), @Index(name="idx_jobs_priority", columnList="priority")})
public class Job {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(name = "cpu_required", nullable = false) private int cpuRequired;
    @Column(name = "ram_required_gb", nullable = false) private int ramRequiredGb;
    @Column(name = "storage_required_gb", nullable = false) private int storageRequiredGb;
    @Column(nullable = false) private int priority;
    @Column(name = "burst_time", nullable = false, columnDefinition = "integer default 1") private int burstTime = 1;
    @Column(nullable = false, length = 20) private String status = "QUEUED";
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();

    protected Job() {}
    public Job(String name, int cpuRequired, int ramRequiredGb, int storageRequiredGb, int priority) {
        this.name = name; this.cpuRequired = cpuRequired; this.ramRequiredGb = ramRequiredGb;
        this.storageRequiredGb = storageRequiredGb; this.priority = priority; this.status = "QUEUED"; this.burstTime = 1;
    }
    public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;}
    public int getCpuRequired(){return cpuRequired;} public void setCpuRequired(int v){cpuRequired=v;}
    public int getRamRequiredGb(){return ramRequiredGb;} public void setRamRequiredGb(int v){ramRequiredGb=v;}
    public int getStorageRequiredGb(){return storageRequiredGb;} public void setStorageRequiredGb(int v){storageRequiredGb=v;}
    public int getPriority(){return priority;} public void setPriority(int v){priority=v;}
    public int getBurstTime(){return burstTime;} public void setBurstTime(int v){burstTime=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
