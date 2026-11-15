package com.cloudscheduler.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="scheduling_runs", indexes=@Index(name="idx_scheduling_runs_created", columnList="created_at"))
public class SchedulingRun {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=20) private String algorithm;
    @Column(nullable=false) private int quantum;
    @Column(nullable=false) private int jobCount;
    @Column(nullable=false) private int allocatedCount;
    @Column(nullable=false) private int waitingCount;
    @Column(nullable=false) private double averageWaitingTime;
    @Column(nullable=false) private double averageTurnaroundTime;
    @Column(nullable=false) private double averageResponseTime;
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    protected SchedulingRun() {}
    public SchedulingRun(String algorithm,int quantum,int jobCount,int allocatedCount,int waitingCount,double aw,double at,double ar){this.algorithm=algorithm;this.quantum=quantum;this.jobCount=jobCount;this.allocatedCount=allocatedCount;this.waitingCount=waitingCount;this.averageWaitingTime=aw;this.averageTurnaroundTime=at;this.averageResponseTime=ar;}
    public Long getId(){return id;} public String getAlgorithm(){return algorithm;} public int getQuantum(){return quantum;} public int getJobCount(){return jobCount;} public int getAllocatedCount(){return allocatedCount;} public int getWaitingCount(){return waitingCount;} public double getAverageWaitingTime(){return averageWaitingTime;} public double getAverageTurnaroundTime(){return averageTurnaroundTime;} public double getAverageResponseTime(){return averageResponseTime;} public LocalDateTime getCreatedAt(){return createdAt;}
}
