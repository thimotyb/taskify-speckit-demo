CREATE TABLE tasks (
    id          UUID PRIMARY KEY,
    project_id  UUID NOT NULL,
    title       VARCHAR(150) NOT NULL,
    description VARCHAR(2000),
    status      VARCHAR(20)  NOT NULL DEFAULT 'TODO'
                CHECK (status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE')),
    assignee_id UUID,
    created_by  UUID NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_by  UUID NOT NULL
);

CREATE INDEX ix_tasks_project_status_created ON tasks (project_id, status, created_at);

CREATE TABLE comments (
    id         UUID PRIMARY KEY,
    task_id    UUID NOT NULL,
    author_id  UUID NOT NULL,
    text       VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_comments_task FOREIGN KEY (task_id) REFERENCES tasks (id)
);

CREATE INDEX ix_comments_task_created ON comments (task_id, created_at);
