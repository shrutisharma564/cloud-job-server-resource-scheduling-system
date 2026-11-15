package com.cloudscheduler.controller;

import com.cloudscheduler.model.Server;
import com.cloudscheduler.repository.ServerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController @RequestMapping("/api/servers")
public class ServerController {
    private final ServerRepository servers;
    public ServerController(ServerRepository servers){this.servers=servers;}
    @GetMapping public List<Server> all(){return servers.findAll();}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Server create(@Valid @RequestBody ServerRequest r){return servers.save(new Server(r.name(),r.totalCpu(),r.totalRamGb(),r.totalStorageGb()));}
    @PatchMapping("/{id}/status") public Server status(@PathVariable Long id,@RequestParam String value){Server s=servers.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Server not found"));String v=value.toUpperCase();if(!List.of("ONLINE","OFFLINE").contains(v))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Status must be ONLINE or OFFLINE");s.setStatus(v);return servers.save(s);}
    public record ServerRequest(@NotBlank String name,@Min(1) int totalCpu,@Min(1) int totalRamGb,@Min(1) int totalStorageGb){}
}
