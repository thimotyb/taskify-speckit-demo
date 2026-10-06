CREATE TABLE users (
    id   UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20)  NOT NULL CHECK (role IN ('PRODUCT_MANAGER', 'ENGINEER'))
);

CREATE TABLE projects (
    id          UUID PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    created_by  UUID NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    -- case-insensitive uniqueness of project names (FR-005)
    name_key    VARCHAR(100) GENERATED ALWAYS AS (LOWER(name))
);

CREATE UNIQUE INDEX ux_projects_name_key ON projects (name_key);
