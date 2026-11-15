package com.cloudscheduler;

import com.cloudscheduler.model.Job;
import com.cloudscheduler.model.Server;
import com.cloudscheduler.repository.JobRepository;
import com.cloudscheduler.repository.ServerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CloudSchedulerApplication {
    public static void main(String[] args) { SpringApplication.run(CloudSchedulerApplication.class, args); }

    @Bean
    CommandLineRunner seedDemoData(ServerRepository servers, JobRepository jobs) {
        return args -> {
            if (servers.count() == 0) {
                servers.save(new Server("Compute Node A", 8, 16, 250));
                servers.save(new Server("Compute Node B", 4, 8, 120));
                servers.save(new Server("Compute Node C", 16, 32, 500));
            }
            if (jobs.count() == 0) {
                Job image = new Job("Image Processing", 2, 4, 20, 2); image.setBurstTime(5);
                Job analytics = new Job("Analytics Batch", 4, 8, 40, 1); analytics.setBurstTime(3);
                Job report = new Job("Report Generation", 1, 2, 10, 3); report.setBurstTime(8);
                jobs.save(image); jobs.save(analytics); jobs.save(report);
            }
        };
    }
}
