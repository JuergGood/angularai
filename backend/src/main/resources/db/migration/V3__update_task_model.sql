-- Update tasks table with new columns for modernization
ALTER TABLE tasks ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE tasks ADD COLUMN completed_at TIMESTAMP;

-- Create table for task tags
CREATE TABLE task_tags (
    task_id BIGINT NOT NULL,
    tag VARCHAR(255),
    CONSTRAINT fk_task_tags_task FOREIGN KEY (task_id) REFERENCES tasks (id)
);
