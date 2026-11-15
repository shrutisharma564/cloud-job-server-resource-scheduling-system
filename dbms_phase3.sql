-- Phase 3 DBMS enhancements for PostgreSQL 16
-- Run after the application has created the JPA tables (jobs, servers, job_allocations, resource_logs).

CREATE INDEX IF NOT EXISTS idx_jobs_status_created ON jobs(status, created_at);
CREATE INDEX IF NOT EXISTS idx_jobs_priority ON jobs(priority);
CREATE INDEX IF NOT EXISTS idx_servers_status ON servers(status);
CREATE INDEX IF NOT EXISTS idx_alloc_status ON job_allocations(status);
CREATE INDEX IF NOT EXISTS idx_alloc_job_status ON job_allocations(job_id, status);
CREATE INDEX IF NOT EXISTS idx_resource_logs_created ON resource_logs(created_at);

CREATE OR REPLACE VIEW active_allocations_view AS
SELECT a.id AS allocation_id, j.id AS job_id, j.name AS job_name,
       s.id AS server_id, s.name AS server_name, a.allocated_at, a.status
FROM job_allocations a
JOIN jobs j ON j.id = a.job_id
JOIN servers s ON s.id = a.server_id
WHERE a.status = 'ACTIVE';

CREATE OR REPLACE FUNCTION log_allocation_change() RETURNS TRIGGER AS $$
DECLARE srv RECORD;
BEGIN
  SELECT id, name, used_cpu, used_ram_gb, used_storage_gb
    INTO srv FROM servers WHERE id = NEW.server_id;
  INSERT INTO resource_logs(server_id, server_name, action, cpu_used, ram_used_gb, storage_used_gb, created_at)
  VALUES (srv.id, srv.name, CASE WHEN TG_OP = 'INSERT' THEN 'ALLOCATION_CREATED' ELSE 'ALLOCATION_UPDATED' END,
          srv.used_cpu, srv.used_ram_gb, srv.used_storage_gb, CURRENT_TIMESTAMP);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_allocation_resource_log ON job_allocations;
CREATE TRIGGER trg_allocation_resource_log
AFTER INSERT OR UPDATE ON job_allocations
FOR EACH ROW EXECUTE FUNCTION log_allocation_change();

CREATE OR REPLACE PROCEDURE mark_job_completed(p_job_id BIGINT)
LANGUAGE plpgsql
AS $$
DECLARE allocation_row RECORD;
BEGIN
  SELECT a.id, a.server_id, j.cpu_required, j.ram_required_gb, j.storage_required_gb
    INTO allocation_row
    FROM job_allocations a JOIN jobs j ON j.id = a.job_id
   WHERE a.job_id = p_job_id AND a.status = 'ACTIVE'
   ORDER BY a.allocated_at DESC LIMIT 1 FOR UPDATE;
  IF NOT FOUND THEN
    RAISE EXCEPTION 'No active allocation for job %', p_job_id;
  END IF;
  UPDATE servers SET
    used_cpu = GREATEST(0, used_cpu - allocation_row.cpu_required),
    used_ram_gb = GREATEST(0, used_ram_gb - allocation_row.ram_required_gb),
    used_storage_gb = GREATEST(0, used_storage_gb - allocation_row.storage_required_gb)
  WHERE id = allocation_row.server_id;
  UPDATE jobs SET status = 'COMPLETED' WHERE id = p_job_id;
  UPDATE job_allocations SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP
   WHERE id = allocation_row.id;
END;
$$;

-- Example DBMS demonstrations:
-- SELECT * FROM active_allocations_view;
-- CALL mark_job_completed(1);
-- SELECT * FROM resource_logs ORDER BY created_at DESC;
