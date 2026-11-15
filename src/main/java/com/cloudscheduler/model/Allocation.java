package com.cloudscheduler.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="job_allocations", indexes = {@Index(name="idx_alloc_status", columnList="status"), @Index(name="idx_alloc_job_status", columnList="job_id,status")})
public class Allocation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="job_id", nullable=false) private Job job;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="server_id", nullable=false) private Server server;
    @Column(nullable=false) private LocalDateTime allocatedAt = LocalDateTime.now();
    private LocalDateTime completedAt;
    @Column(nullable=false, length=20) private String status="ACTIVE";
    protected Allocation(){}
    public Allocation(Job job, Server server){this.job=job;this.server=server;}
    public Long getId(){return id;} public Job getJob(){return job;} public Server getServer(){return server;}
    public LocalDateTime getAllocatedAt(){return allocatedAt;} public LocalDateTime getCompletedAt(){return completedAt;}
    public void setCompletedAt(LocalDateTime v){completedAt=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
