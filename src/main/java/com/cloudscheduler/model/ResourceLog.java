package com.cloudscheduler.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="resource_logs", indexes=@Index(name="idx_resource_logs_created", columnList="created_at"))
public class ResourceLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Long serverId;
    @Column(nullable=false, length=100) private String serverName;
    @Column(nullable=false, length=30) private String action;
    @Column(nullable=false) private int cpuUsed;
    @Column(nullable=false) private int ramUsedGb;
    @Column(nullable=false) private int storageUsedGb;
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    protected ResourceLog() {}
    public ResourceLog(Server s,String action){this.serverId=s.getId();this.serverName=s.getName();this.action=action;this.cpuUsed=s.getUsedCpu();this.ramUsedGb=s.getUsedRamGb();this.storageUsedGb=s.getUsedStorageGb();}
    public Long getId(){return id;} public Long getServerId(){return serverId;} public String getServerName(){return serverName;} public String getAction(){return action;} public int getCpuUsed(){return cpuUsed;} public int getRamUsedGb(){return ramUsedGb;} public int getStorageUsedGb(){return storageUsedGb;} public LocalDateTime getCreatedAt(){return createdAt;}
}
