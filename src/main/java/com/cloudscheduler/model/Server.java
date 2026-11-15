package com.cloudscheduler.model;

import jakarta.persistence.*;

@Entity
@Table(name = "servers", indexes = {@Index(name="idx_servers_status", columnList="status")})
public class Server {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String name;
    @Column(name = "total_cpu", nullable = false) private int totalCpu;
    @Column(name = "total_ram_gb", nullable = false) private int totalRamGb;
    @Column(name = "total_storage_gb", nullable = false) private int totalStorageGb;
    @Column(name = "used_cpu", nullable = false) private int usedCpu = 0;
    @Column(name = "used_ram_gb", nullable = false) private int usedRamGb = 0;
    @Column(name = "used_storage_gb", nullable = false) private int usedStorageGb = 0;
    @Column(nullable = false, length = 20) private String status = "ONLINE";

    protected Server() {}
    public Server(String name, int totalCpu, int totalRamGb, int totalStorageGb) {
        this.name=name; this.totalCpu=totalCpu; this.totalRamGb=totalRamGb; this.totalStorageGb=totalStorageGb; this.status="ONLINE";
    }
    public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;}
    public int getTotalCpu(){return totalCpu;} public int getTotalRamGb(){return totalRamGb;} public int getTotalStorageGb(){return totalStorageGb;}
    public int getUsedCpu(){return usedCpu;} public int getUsedRamGb(){return usedRamGb;} public int getUsedStorageGb(){return usedStorageGb;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public int getAvailableCpu(){return totalCpu-usedCpu;} public int getAvailableRamGb(){return totalRamGb-usedRamGb;}
    public int getAvailableStorageGb(){return totalStorageGb-usedStorageGb;}
    public boolean canFit(Job j){return "ONLINE".equals(status) && getAvailableCpu()>=j.getCpuRequired() && getAvailableRamGb()>=j.getRamRequiredGb() && getAvailableStorageGb()>=j.getStorageRequiredGb();}
    public void allocate(Job j){usedCpu+=j.getCpuRequired(); usedRamGb+=j.getRamRequiredGb(); usedStorageGb+=j.getStorageRequiredGb();}
    public void release(Job j){usedCpu=Math.max(0,usedCpu-j.getCpuRequired()); usedRamGb=Math.max(0,usedRamGb-j.getRamRequiredGb()); usedStorageGb=Math.max(0,usedStorageGb-j.getStorageRequiredGb());}
}
