CREATE TABLE job_application (
    id            BIGSERIAL PRIMARY KEY,
    company       VARCHAR(200)  NOT NULL,
    job_role      VARCHAR(200)  NOT NULL,
    canonical_url VARCHAR(1000) NOT NULL,
    status        VARCHAR(40)   NOT NULL,
    match_score   INTEGER,
    applied_on    DATE,
    created_at    TIMESTAMP     NOT NULL,
    updated_at    TIMESTAMP     NOT NULL,
    version       BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uq_job_application_canonical_url UNIQUE (canonical_url),
    CONSTRAINT ck_job_application_match_score CHECK (match_score IS NULL OR (match_score BETWEEN 0 AND 100))
);

-- The list endpoint filters by status and orders by recency.
CREATE INDEX ix_job_application_status_updated ON job_application (status, updated_at DESC);
