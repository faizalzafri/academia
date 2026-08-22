package com.academia.platform.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.academia.platform.model.ApprovalStatus;
import com.academia.platform.model.Student;
import com.academia.platform.model.Teacher;
import com.academia.platform.model.User;
import com.academia.platform.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Seed or update admin user
        seedUser("admin", "admin@academia.edu", "Password123!", "System Administrator",
                Set.of("SYSTEM_ADMIN", "PRINCIPAL", "TEACHER", "USER"));

        // Seed principal user
        seedTeacher("principal", "principal@academia.edu", "Password123!", "Dr. Arthur Vance",
                "EMP-PRIN-01", "Principal / Headmaster", "Administration", "Educational Leadership",
                Set.of("PRINCIPAL", "TEACHER", "USER"));

        // Seed sample faculty user
        seedTeacher("teacher1", "teacher1@academia.edu", "Password123!", "Prof. Sarah Connor",
                "EMP-MATH-01", "Senior Lecturer", "Mathematics", "Applied Algebra",
                Set.of("TEACHER", "USER"));

        // Seed sample student user
        seedStudent("student1", "student1@academia.edu", "Password123!", "Alex Morgan",
                "Computer Science", "Software Engineering", "2004-05-15",
                com.academia.platform.model.Gender.FEMALE, "2024-2028", "Dr. Vance",
                Set.of("USER"));

        logger.info("Default users initialized successfully.");
    }

    private void seedUser(String username, String email, String rawPassword, String desc, Set<String> roles) {
        User user = userRepository.findById(username).orElse(new User());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setDescription(desc);
        user.setRegistrationDate(LocalDateTime.now());
        user.setAuthorities(new HashSet<>(roles));
        userRepository.save(user);
    }

    private void seedTeacher(String username, String email, String rawPassword, String name,
                             String empId, String designation, String dept, String spec, Set<String> roles) {
        Teacher teacher = (Teacher) userRepository.findById(username).filter(u -> u instanceof Teacher).orElse(new Teacher());
        teacher.setUsername(username);
        teacher.setEmail(email);
        teacher.setPassword(passwordEncoder.encode(rawPassword));
        teacher.setEnabled(true);
        teacher.setApprovalStatus(ApprovalStatus.APPROVED);
        teacher.setName(name);
        teacher.setEmployeeId(empId);
        teacher.setDesignation(designation);
        teacher.setDepartment(dept);
        teacher.setSpecialization(spec);
        teacher.setDescription(designation + " in " + dept);
        teacher.setRegistrationDate(LocalDateTime.now());
        teacher.setAuthorities(new HashSet<>(roles));
        userRepository.save(teacher);
    }

    private void seedStudent(String username, String email, String rawPassword, String name,
                             String dept, String major, String dob, com.academia.platform.model.Gender gender, String cohort, String advisor, Set<String> roles) {
        Student student = (Student) userRepository.findById(username).filter(u -> u instanceof Student).orElse(new Student());
        student.setUsername(username);
        student.setEmail(email);
        student.setPassword(passwordEncoder.encode(rawPassword));
        student.setEnabled(true);
        student.setApprovalStatus(ApprovalStatus.APPROVED);
        student.setName(name);
        student.setDepartment(dept);
        student.setMajor(major);
        student.setDateOfBirth(dob);
        student.setGender(gender);
        student.setCohort(cohort);
        student.setAcademicAdvior(advisor);
        student.setDescription("Student - " + major);
        student.setRegistrationDate(LocalDateTime.now());
        student.setAuthorities(new HashSet<>(roles));
        userRepository.save(student);
    }
}
