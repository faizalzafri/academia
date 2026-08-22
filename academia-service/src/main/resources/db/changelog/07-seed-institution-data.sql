--liquibase formatted sql

--changeset academia:07-seed-institution-data splitStatements:false dbms:postgresql
DO $$
DECLARE
    v_academic_year_id BIGINT;
    v_class_idx INT;
    v_class_db_id BIGINT;
    v_sec_idx INT;
    v_sec_db_id BIGINT;
    v_sec_letter CHAR(1);
    v_sec_upper CHAR(1);
    v_teacher_username VARCHAR(50);
    v_class_section_id BIGINT;
    v_subject_record RECORD;
    v_period_num INT;
    v_start_time VARCHAR(10);
    v_end_time VARCHAR(10);
    v_student_num INT;
    v_student_username VARCHAR(50);
    v_student_name VARCHAR(100);
BEGIN
    -- 1. Ensure Academic Year 2026-2027 exists
    SELECT id INTO v_academic_year_id FROM academic_years WHERE name = '2026-2027';
    IF v_academic_year_id IS NULL THEN
        INSERT INTO academic_years (name, start_date, end_date, status, created_at)
        VALUES ('2026-2027', '2026-04-01', '2027-03-31', 'ACTIVE', '2026-04-01 00:00:00')
        RETURNING id INTO v_academic_year_id;
    END IF;

    -- 2. Ensure Classes 1 through 12 exist
    FOR v_class_idx IN 1..12 LOOP
        INSERT INTO school_classes (class_name)
        VALUES ('Class ' || v_class_idx)
        ON CONFLICT (class_name) DO NOTHING;
    END LOOP;

    -- 3. Ensure Sections A, B, C exist
    INSERT INTO sections (section_name) VALUES ('A') ON CONFLICT (section_name) DO NOTHING;
    INSERT INTO sections (section_name) VALUES ('B') ON CONFLICT (section_name) DO NOTHING;
    INSERT INTO sections (section_name) VALUES ('C') ON CONFLICT (section_name) DO NOTHING;

    -- 4. Ensure Core Subjects exist
    INSERT INTO subjects (name, code) VALUES ('Mathematics', 'MATH-01') ON CONFLICT (code) DO NOTHING;
    INSERT INTO subjects (name, code) VALUES ('Physics', 'PHYS-01') ON CONFLICT (code) DO NOTHING;
    INSERT INTO subjects (name, code) VALUES ('Chemistry', 'CHEM-01') ON CONFLICT (code) DO NOTHING;
    INSERT INTO subjects (name, code) VALUES ('Computer Science', 'CS-01') ON CONFLICT (code) DO NOTHING;
    INSERT INTO subjects (name, code) VALUES ('English', 'ENG-01') ON CONFLICT (code) DO NOTHING;

    -- 5. Iterate through Classes and Sections to seed Teachers, Class Sections, Timetables, and Students
    FOR v_class_idx IN 1..12 LOOP
        SELECT id INTO v_class_db_id FROM school_classes WHERE class_name = 'Class ' || v_class_idx;

        FOR v_sec_idx IN 1..3 LOOP
            v_sec_letter := CASE v_sec_idx WHEN 1 THEN 'a' WHEN 2 THEN 'b' ELSE 'c' END;
            v_sec_upper  := CASE v_sec_idx WHEN 1 THEN 'A' WHEN 2 THEN 'B' ELSE 'C' END;

            SELECT id INTO v_sec_db_id FROM sections WHERE section_name = v_sec_upper;

            v_teacher_username := 't_c' || v_class_idx || '_' || v_sec_letter;

            -- Create Class Teacher Account
            INSERT INTO users (
                username, user_type, password, enabled, email, teacher_name, 
                employee_id, designation, department, specialization, approval_status, registration_date
            )
            VALUES (
                v_teacher_username,
                'teacher',
                '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6',
                true,
                v_teacher_username || '@academia.edu',
                'Teacher C' || v_class_idx || '-' || v_sec_upper,
                'EMP' || (100 + (v_class_idx - 1) * 3 + v_sec_idx),
                'Class Teacher',
                'Academics',
                'General Education',
                'APPROVED',
                NOW()
            )
            ON CONFLICT (username) DO NOTHING;

            -- Assign Teacher Authority
            INSERT INTO authorities (users_username, authorities)
            SELECT v_teacher_username, 'TEACHER'
            WHERE NOT EXISTS (
                SELECT 1 FROM authorities WHERE users_username = v_teacher_username AND authorities = 'TEACHER'
            );

            INSERT INTO authorities (users_username, authorities)
            SELECT v_teacher_username, 'USER'
            WHERE NOT EXISTS (
                SELECT 1 FROM authorities WHERE users_username = v_teacher_username AND authorities = 'USER'
            );

            -- Create or Retrieve Class Section
            SELECT id INTO v_class_section_id
            FROM class_sections
            WHERE class_id = v_class_db_id AND section_id = v_sec_db_id AND academic_year_id = v_academic_year_id;

            IF v_class_section_id IS NULL THEN
                INSERT INTO class_sections (class_id, section_id, class_teacher_username, academic_year_id)
                VALUES (v_class_db_id, v_sec_db_id, v_teacher_username, v_academic_year_id)
                RETURNING id INTO v_class_section_id;
            ELSE
                UPDATE class_sections
                SET class_teacher_username = v_teacher_username
                WHERE id = v_class_section_id AND (class_teacher_username IS NULL OR class_teacher_username = '');
            END IF;

            -- Generate Timetable Slots (5 standard periods)
            v_period_num := 0;
            FOR v_subject_record IN (SELECT id FROM subjects ORDER BY id ASC LIMIT 5) LOOP
                v_period_num := v_period_num + 1;

                v_start_time := CASE v_period_num
                    WHEN 1 THEN '08:00'
                    WHEN 2 THEN '09:00'
                    WHEN 3 THEN '10:00'
                    WHEN 4 THEN '11:00'
                    WHEN 5 THEN '12:00'
                END;

                v_end_time := CASE v_period_num
                    WHEN 1 THEN '08:50'
                    WHEN 2 THEN '09:50'
                    WHEN 3 THEN '10:50'
                    WHEN 4 THEN '11:50'
                    WHEN 5 THEN '12:50'
                END;

                IF NOT EXISTS (
                    SELECT 1 FROM timetable_slots 
                    WHERE class_section_id = v_class_section_id 
                      AND day_of_week = 'MONDAY' 
                      AND period_number = v_period_num 
                      AND academic_year_id = v_academic_year_id
                ) THEN
                    INSERT INTO timetable_slots (
                        class_section_id, subject_id, teacher_username, day_of_week, 
                        period_number, start_time, end_time, academic_year_id
                    )
                    VALUES (
                        v_class_section_id, v_subject_record.id, v_teacher_username, 'MONDAY', 
                        v_period_num, v_start_time, v_end_time, v_academic_year_id
                    );
                END IF;
            END LOOP;

            -- Generate 30 Students & Enrollments for this section
            FOR v_student_num IN 1..30 LOOP
                v_student_username := 's_c' || v_class_idx || '_' || v_sec_letter || '_st' || LPAD(v_student_num::TEXT, 2, '0');
                v_student_name := 'Student C' || v_class_idx || v_sec_upper || ' ' || v_student_num;

                -- Insert Student User
                INSERT INTO users (
                    username, user_type, password, enabled, email, student_name, 
                    gender, date_of_birth, major, department, cohort, approval_status, registration_date
                )
                VALUES (
                    v_student_username,
                    'student',
                    '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6',
                    true,
                    v_student_username || '@academia.edu',
                    v_student_name,
                    CASE WHEN v_student_num % 2 = 0 THEN 'FEMALE' ELSE 'MALE' END,
                    '2010-05-15',
                    'General',
                    'High School',
                    '2026',
                    'APPROVED',
                    NOW()
                )
                ON CONFLICT (username) DO NOTHING;

                -- Assign User Authority
                INSERT INTO authorities (users_username, authorities)
                SELECT v_student_username, 'USER'
                WHERE NOT EXISTS (
                    SELECT 1 FROM authorities WHERE users_username = v_student_username AND authorities = 'USER'
                );

                -- Create Student Enrollment
                IF NOT EXISTS (
                    SELECT 1 FROM student_enrollments 
                    WHERE student_username = v_student_username AND academic_year_id = v_academic_year_id
                ) THEN
                    INSERT INTO student_enrollments (
                        student_username, class_section_id, academic_year_id, roll_number, status, enrolled_at
                    )
                    VALUES (
                        v_student_username,
                        v_class_section_id,
                        v_academic_year_id,
                        'C' || v_class_idx || v_sec_upper || '-' || LPAD(v_student_num::TEXT, 2, '0'),
                        'ENROLLED',
                        NOW()
                    );
                END IF;

            END LOOP; -- End Student Loop

        END LOOP; -- End Section Loop
    END LOOP; -- End Class Loop

END $$;
