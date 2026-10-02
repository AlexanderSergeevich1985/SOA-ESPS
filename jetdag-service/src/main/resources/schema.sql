-- 1. Main configuration table for pipelines/DAGs
CREATE TABLE IF NOT EXISTS pipelines (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,          -- Unique name of the DAG (Workflow ID)
    cron_schedule VARCHAR(50),                  -- Cron expressions (e.g. '0 0 * * *'), NULL if manual only
    is_active BOOLEAN NOT NULL DEFAULT true,    -- Active flag for the daemon scheduler
    max_concurrent_runs INT DEFAULT 1,          -- Prevents overlapping run states (Airflow parity)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tasks table describing atomic endpoints invocation
CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY,
    pipeline_id UUID NOT NULL REFERENCES pipelines(id) ON DELETE CASCADE,
    task_code VARCHAR(100) NOT NULL,            -- String identifier within the DAG scope (e.g., 'fetch_orders')
    operator_type VARCHAR(50) NOT NULL,         -- Executor type (e.g., 'HTTP', 'BASH')
    params_json TEXT NOT NULL,                  -- Contains url, method, and payload templates
    fallback_task_code VARCHAR(100),            -- Target task_code to execute if this task hits a failure
    timeout_seconds INT DEFAULT 3600,           -- Activity timeout safeguard
    retry_attempts INT DEFAULT 3,               -- Automated retry limits natively handled by Temporal
    retry_backoff_seconds INT DEFAULT 5,        -- Backoff duration between individual step execution retries

    UNIQUE(pipeline_id, task_code)
);
CREATE INDEX IF NOT EXISTS idx_tasks_pipeline_id ON tasks(pipeline_id);

-- 3. Relationships table representing Directed Acyclic Graph topology (Many-to-Many mesh)
CREATE TABLE IF NOT EXISTS task_dependencies (
    id BIGSERIAL PRIMARY KEY,
    pipeline_id UUID NOT NULL REFERENCES pipelines(id) ON DELETE CASCADE,
    upstream_task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,   -- Parent node (must finish first)
    downstream_task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE, -- Child node (waits for parent)

    UNIQUE(pipeline_id, upstream_task_id, downstream_task_id),
    CONSTRAINT check_self_dependency CHECK (upstream_task_id <> downstream_task_id)
);
CREATE INDEX IF NOT EXISTS idx_dependencies_pipeline_id ON task_dependencies(pipeline_id);