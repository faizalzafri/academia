--liquibase formatted sql

--changeset academia:02-add-approval-columns-to-users
ALTER TABLE users ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20) DEFAULT 'APPROVED' NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS registration_date TIMESTAMP;
