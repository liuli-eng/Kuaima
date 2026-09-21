CREATE TABLE IF NOT EXISTS scheduled_job_execution (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    job_code VARCHAR(80) NOT NULL,
    period_key VARCHAR(80) NULL,
    status VARCHAR(20) NOT NULL,
    started_at DATETIME NULL,
    finished_at DATETIME NULL,
    total_count INT NULL,
    success_count INT NULL,
    failure_count INT NULL,
    error_message VARCHAR(2000) NULL,
    instance_id VARCHAR(100) NULL,
    trigger_type VARCHAR(20) NOT NULL,
    KEY idx_scheduled_job_execution_code_time (job_code, started_at)
);
