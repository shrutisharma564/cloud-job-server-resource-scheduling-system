package com.cloudscheduler.dto;
import java.time.LocalDateTime;
public record AllocationView(Long id, Long jobId, String jobName, Long serverId, String serverName,
                             LocalDateTime allocatedAt, LocalDateTime completedAt, String status) {}
