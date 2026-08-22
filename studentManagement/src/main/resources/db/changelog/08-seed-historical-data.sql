--liquibase formatted sql

--changeset academia:08-seed-historical-data splitStatements:false dbms:postgresql
DO $$
DECLARE
    v_year_2425_id BIGINT;
    v_year_2526_id BIGINT;
    v_class_idx INT;
    v_class_db_id BIGINT;
    v_sec_idx INT;
    v_sec_db_id BIGINT;
    v_sec_letter CHAR(1);
    v_sec_upper CHAR(1);
    v_teacher_username VARCHAR(50);
    v_cs_2425_id BIGINT;
    v_cs_2526_id BIGINT;
    v_student_num INT;
    v_student_username VARCHAR(50);
    v_student_name VARCHAR(100);
    v_status VARCHAR(20);
BEGIN
    -- 1. Create Historical Academic Year 2024-2025 (COMPLETED)
    SELECT id INTO v_year_2425_id FROM academic_years WHERE name = '2024-2025';
    IF v_year_2425_id IS NULL THEN
        INSERT INTO academic_years (name, start_date, end_date, status, created_at, completed_at, created_by)
        VALUES ('2024-2025', '2024-04-01', '2025-03-31', 'COMPLETED', '2024-04-01 00:00:00', '2025-03-31 23:59:59', 'admin')
        RETURNING id INTO v_year_2425_id;
    ELSE
        UPDATE academic_years 
        SET status = 'COMPLETED', completed_at = '2025-03-31 23:59:59' 
        WHERE id = v_year_2425_id;
    END IF;

    -- 2. Create Historical Academic Year 2025-2026 (COMPLETED)
    SELECT id INTO v_year_2526_id FROM academic_years WHERE name = '2025-2026';
    IF v_year_2526_id IS NULL THEN
        INSERT INTO academic_years (name, start_date, end_date, status, created_at, completed_at, created_by)
        VALUES ('2025-2026', '2025-04-01', '2026-03-31', 'COMPLETED', '2025-04-01 00:00:00', '2026-03-31 23:59:59', 'admin')
        RETURNING id INTO v_year_2526_id;
    ELSE
        UPDATE academic_years 
        SET status = 'COMPLETED', completed_at = '2026-03-31 23:59:59' 
        WHERE id = v_year_2526_id;
    END IF;

    -- 3. Iterate through Classes (1 to 12) and Sections (A, B, C) to build Class Sections & Historical Enrollments
    FOR v_class_idx IN 1..12 LOOP
        SELECT id INTO v_class_db_id FROM school_classes WHERE class_name = 'Class ' || v_class_idx;

        FOR v_sec_idx IN 1..3 LOOP
            v_sec_letter := CASE v_sec_idx WHEN 1 THEN 'a' WHEN 2 THEN 'b' ELSE 'c' END;
            v_sec_upper  := CASE v_sec_idx WHEN 1 THEN 'A' WHEN 2 THEN 'B' ELSE 'C' END;

            SELECT id INTO v_sec_db_id FROM sections WHERE section_name = v_sec_upper;
            v_teacher_username := 't_c' || v_class_idx || '_' || v_sec_letter;

            -- 3a. Class Section for 2024-2025
            SELECT id INTO v_cs_2425_id 
            FROM class_sections 
            WHERE class_id = v_class_db_id AND section_id = v_sec_db_id AND academic_year_id = v_year_2425_id;

            IF v_cs_2425_id IS NULL THEN
                INSERT INTO class_sections (class_id, section_id, class_teacher_username, academic_year_id)
                VALUES (v_class_db_id, v_sec_db_id, v_teacher_username, v_year_2425_id)
                RETURNING id INTO v_cs_2425_id;
            END IF;

            -- 3b. Class Section for 2025-2026
            SELECT id INTO v_cs_2526_id 
            FROM class_sections 
            WHERE class_id = v_class_db_id AND section_id = v_sec_db_id AND academic_year_id = v_year_2526_id;

            IF v_cs_2526_id IS NULL THEN
                INSERT INTO class_sections (class_id, section_id, class_teacher_username, academic_year_id)
                VALUES (v_class_db_id, v_sec_db_id, v_teacher_username, v_year_2526_id)
                RETURNING id INTO v_cs_2526_id;
            END IF;

            -- 4. Generate 30 Students & Enrollments per section for 2024-2025 and 2025-2026
            FOR v_student_num IN 1..30 LOOP

                -- Determine student identity for 2024-2025
                IF v_class_idx = 12 THEN
                    -- Graduated Alumni Class of 2025
                    v_student_username := 's_alum25_c12' || v_sec_letter || '_st' || LPAD(v_student_num::TEXT, 2, '0');
                    v_student_name := 'Alumni 2025 C12' || v_sec_upper || ' ' || v_student_num;
                    v_status := 'GRADUATED';
                ELSIF v_class_idx = 11 THEN
                    -- Students who became Class 12 in 2025-2026 and graduated in 2026
                    v_student_username := 's_alum26_c12' || v_sec_letter || '_st' || LPAD(v_student_num::TEXT, 2, '0');
                    v_student_name := 'Alumni 2026 C12' || v_sec_upper || ' ' || v_student_num;
                    v_status := 'PROMOTED';
                ELSE
                    -- Progressive students who advanced to Class (v_class_idx + 2) in 2026-2027
                    v_student_username := 's_c' || (v_class_idx + 2) || '_' || v_sec_letter || '_st' || LPAD(v_student_num::TEXT, 2, '0');
                    v_student_name := 'Student C' || (v_class_idx + 2) || v_sec_upper || ' ' || v_student_num;
                    v_status := 'PROMOTED';
                END IF;

                -- Ensure student user exists
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
                    '2008-05-15',
                    'General',
                    'High School',
                    '2024',
                    'APPROVED',
                    '2024-04-01 09:00:00'
                )
                ON CONFLICT (username) DO NOTHING;

                -- Ensure user authority
                INSERT INTO authorities (users_username, authorities)
                SELECT v_student_username, 'USER'
                WHERE NOT EXISTS (
                    SELECT 1 FROM authorities WHERE users_username = v_student_username AND authorities = 'USER'
                );

                -- Insert 2024-2025 Enrollment Record
                IF NOT EXISTS (
                    SELECT 1 FROM student_enrollments 
                    WHERE student_username = v_student_username AND academic_year_id = v_year_2425_id
                ) THEN
                    INSERT INTO student_enrollments (
                        student_username, class_section_id, academic_year_id, roll_number, status, enrolled_at
                    )
                    VALUES (
                        v_student_username,
                        v_cs_2425_id,
                        v_year_2425_id,
                        'C' || v_class_idx || v_sec_upper || '-' || LPAD(v_student_num::TEXT, 2, '0'),
                        v_status,
                        '2024-04-05 09:00:00'
                    );
                END IF;

                -- Determine student identity for 2025-2026
                IF v_class_idx = 12 THEN
                    -- Graduated Alumni Class of 2026
                    v_student_username := 's_alum26_c12' || v_sec_letter || '_st' || LPAD(v_student_num::TEXT, 2, '0');
                    v_student_name := 'Alumni 2026 C12' || v_sec_upper || ' ' || v_student_num;
                    v_status := 'GRADUATED';
                ELSE
                    -- Progressive students who advanced to Class (v_class_idx + 1) in 2026-2027
                    v_student_username := 's_c' || (v_class_idx + 1) || '_' || v_sec_letter || '_st' || LPAD(v_student_num::TEXT, 2, '0');
                    v_student_name := 'Student C' || (v_class_idx + 1) || v_sec_upper || ' ' || v_student_num;
                    v_status := 'PROMOTED';
                END IF;

                -- Ensure student user exists
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
                    '2009-05-15',
                    'General',
                    'High School',
                    '2025',
                    'APPROVED',
                    '2025-04-01 09:00:00'
                )
                ON CONFLICT (username) DO NOTHING;

                -- Ensure user authority
                INSERT INTO authorities (users_username, authorities)
                SELECT v_student_username, 'USER'
                WHERE NOT EXISTS (
                    SELECT 1 FROM authorities WHERE users_username = v_student_username AND authorities = 'USER'
                );

                -- Insert 2025-2026 Enrollment Record
                IF NOT EXISTS (
                    SELECT 1 FROM student_enrollments 
                    WHERE student_username = v_student_username AND academic_year_id = v_year_2526_id
                ) THEN
                    INSERT INTO student_enrollments (
                        student_username, class_section_id, academic_year_id, roll_number, status, enrolled_at
                    )
                    VALUES (
                        v_student_username,
                        v_cs_2526_id,
                        v_year_2526_id,
                        'C' || v_class_idx || v_sec_upper || '-' || LPAD(v_student_num::TEXT, 2, '0'),
                        v_status,
                        '2025-04-05 09:00:00'
                    );
                END IF;

            END LOOP; -- End student loop

        END LOOP; -- End section loop
    END LOOP; -- End class loop

END $$;
