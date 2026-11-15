# Cloud Job & Server Resource Scheduling System — Phase 3

A Phase 3 extension of the uploaded Phase 2 (50%) baseline. It keeps the Spring Boot + PostgreSQL + Docker architecture and adds OS scheduling simulation, resource-safety algorithms, scheduling history, and resource audit logging.

## Phase 2 baseline retained
- Create/list jobs and servers.
- FCFS and Priority scheduling.
- CPU/RAM/storage fit checks and server allocation.
- Complete jobs and release allocated resources.
- PostgreSQL persistence and browser dashboard.

## Phase 3 additions
### Operating Systems
- **SJF (non-preemptive)** scheduling.
- **Round Robin (preemptive simulation)** with configurable time quantum.
- CPU burst-time field for each job.
- Gantt timeline segments and per-job waiting, turnaround, response and completion times.
- Average waiting/turnaround/response times and throughput.
- **Banker's Algorithm** safety check with Need matrix and safe sequence.
- **Deadlock detection** using Available, Allocation and Request matrices.
- Resource allocation/release audit records.

### DBMS
- `scheduling_runs` table stores each algorithm run and summary metrics.
- `resource_logs` table stores server resource snapshots for allocation/release events.
- JPA indexes for common job, server, allocation and history lookups.
- `dbms_phase3.sql` adds additional indexes, `active_allocations_view`, an allocation audit trigger and `mark_job_completed` stored procedure.
- Existing JPA transactions continue to wrap scheduling and job completion operations.

## Run on Windows (Docker Desktop required)
1. Extract the ZIP.
2. Open PowerShell in the extracted `cloud-scheduler-phase2-50` folder (the folder containing `pom.xml` and `docker-compose.yml`).
3. Run:

```powershell
docker compose up --build -d
```

4. Check containers:

```powershell
docker ps
```

5. Open `http://localhost:8080`.

6. View logs:

```powershell
docker compose logs -f app
```

Stop with `docker compose down`. Keep the named volume to preserve database data. `docker compose down -v` deletes the database volume and all local project data.

## Scheduling demo
1. Add at least 3 jobs with different CPU burst times and priorities.
2. Select FCFS, SJF, Priority or Round Robin in the scheduling dropdown.
3. For Round Robin, set a time quantum (e.g. 2).
4. Click **Run Scheduler** to see the Gantt sequence and CPU metrics.
5. Complete an allocated job to release its server resources.
6. Use the history panel to view scheduling runs and resource logs.

All jobs are simulated CPU workloads; the application does not start arbitrary processes on the host operating system. Waiting/turnaround/response metrics assume all queued jobs arrive at time 0. Scheduling run history records the CPU simulation and resource allocation outcome.

## OS resource safety lab
The dashboard includes editable JSON examples for:
- `POST /api/resources/bankers-check`
- `POST /api/resources/deadlock-check`

Banker's request format:

```json
{
  "processNames": ["P0", "P1"],
  "available": [3, 2],
  "allocation": [[1, 0], [0, 1]],
  "maximum": [[2, 1], [1, 2]]
}
```

Deadlock request format uses the same `processNames`, `available` and `allocation` fields plus a `request` matrix instead of `maximum`.

## API quick reference
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/jobs` | List jobs |
| POST | `/api/jobs` | Create job (includes optional `burstTime`) |
| GET | `/api/servers` | List servers |
| POST | `/api/servers` | Add server |
| POST | `/api/scheduling/run?algorithm=FCFS` | Run FCFS |
| POST | `/api/scheduling/run?algorithm=SJF` | Run SJF |
| POST | `/api/scheduling/run?algorithm=PRIORITY` | Run Priority |
| POST | `/api/scheduling/run?algorithm=ROUND_ROBIN&quantum=2` | Run Round Robin |
| GET | `/api/scheduling/history` | Scheduling history |
| GET | `/api/resources/history` | Resource audit history |
| POST | `/api/resources/bankers-check` | Banker's safety check |
| POST | `/api/resources/deadlock-check` | Deadlock detection |
| GET | `/api/allocations` | Allocation history |
| POST | `/api/jobs/{jobId}/complete` | Complete job and release resources |

## DBMS SQL enhancements
After the application has started once (so Hibernate has created the entity tables), run `dbms_phase3.sql` against the `cloud_scheduler` PostgreSQL database using pgAdmin or `psql`. It creates the view, trigger, stored procedure and additional indexes. Example statements are commented at the bottom of the SQL file.

The stored procedure `mark_job_completed` is a database-side demonstration. Do not call it for a job that has already been completed through the REST API, because both paths release resources.

## Important implementation scope
- This is an educational simulation, not a production cloud scheduler.
- Round Robin time-slices CPU burst simulation; server resource allocation is recorded once per job based on its first appearance in the Round Robin sequence.
- Banker's and deadlock detection accept resource matrices supplied by the user; they are algorithm demonstrations and are not yet automatically coupled to the server allocation engine.
- Authentication, production secrets management, real worker processes, cloud hosting/CI-CD and production monitoring are not included.
- Docker Compose credentials are local-development credentials only.
