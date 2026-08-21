--liquibase formatted sql

--changeset academia:03-seed-system-admin
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT count(*) FROM users WHERE username = 'admin'
INSERT INTO users (username, user_type, password, enabled, email, description, approval_status)
VALUES ('admin', 'user', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', TRUE, 'admin@academia.edu', 'System Administrator Account', 'APPROVED');

INSERT INTO authorities (users_username, authorities) VALUES ('admin', 'SYSTEM_ADMIN');
INSERT INTO authorities (users_username, authorities) VALUES ('admin', 'TEACHER');
INSERT INTO authorities (users_username, authorities) VALUES ('admin', 'PRINCIPAL');
INSERT INTO authorities (users_username, authorities) VALUES ('admin', 'USER');

--changeset academia:03-seed-reference-classes-sections-subjects
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT count(*) FROM school_classes
INSERT INTO school_classes (class_name) VALUES ('Class 9');
INSERT INTO school_classes (class_name) VALUES ('Class 10');
INSERT INTO school_classes (class_name) VALUES ('Class 11');
INSERT INTO school_classes (class_name) VALUES ('Class 12');

INSERT INTO sections (section_name) VALUES ('A');
INSERT INTO sections (section_name) VALUES ('B');

INSERT INTO subjects (name, code) VALUES ('Mathematics', 'MATH-01');
INSERT INTO subjects (name, code) VALUES ('Physics', 'PHYS-01');
INSERT INTO subjects (name, code) VALUES ('Chemistry', 'CHEM-01');
INSERT INTO subjects (name, code) VALUES ('Computer Science', 'CS-01');
INSERT INTO subjects (name, code) VALUES ('English', 'ENG-01');

INSERT INTO class_sections (class_id, section_id) VALUES (1, 1);
INSERT INTO class_sections (class_id, section_id) VALUES (1, 2);
