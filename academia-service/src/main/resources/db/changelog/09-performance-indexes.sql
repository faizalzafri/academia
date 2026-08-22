--liquibase formatted sql

--changeset academia:09-create-performance-indexes
CREATE INDEX IF NOT EXISTS idx_enrollments_ay_cs ON student_enrollments(academic_year_id, class_section_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_ay_status ON student_enrollments(academic_year_id, status);
CREATE INDEX IF NOT EXISTS idx_enrollments_student ON student_enrollments(student_username);
CREATE INDEX IF NOT EXISTS idx_enrollments_roll ON student_enrollments(roll_number);

CREATE INDEX IF NOT EXISTS idx_cs_ay_class_sec ON class_sections(academic_year_id, class_id, section_id);
CREATE INDEX IF NOT EXISTS idx_cs_teacher ON class_sections(class_teacher_username);

CREATE INDEX IF NOT EXISTS idx_timetable_cs_ay ON timetable_slots(class_section_id, academic_year_id);
CREATE INDEX IF NOT EXISTS idx_timetable_teacher_ay ON timetable_slots(teacher_username, academic_year_id);
CREATE INDEX IF NOT EXISTS idx_timetable_conflict_check ON timetable_slots(teacher_username, day_of_week, period_number);

CREATE INDEX IF NOT EXISTS idx_events_date_ay ON school_calendar_events(event_date, academic_year_id);
