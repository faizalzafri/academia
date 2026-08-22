package com.academia.platform.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity(name = "teacher")
@DiscriminatorValue("teacher")
public class Teacher extends User {
    @Column(name = "teacher_name", length = 50)
    private String name;

    @Column(name = "employee_id", length = 20)
    private String employeeId;

    @Column(length = 50)
    private String designation;

    @Column(length = 50)
    private String department;

    @Column(length = 50)
    private String specialization;

    // Default constructor
    public Teacher() {
        super();
    }

    // Constructor with parameters
    public Teacher(String username, String email, String password, String name, String employeeId, String designation, String specialization) {
        super(email, username, password, true, null, "");
        this.name = name;
        this.employeeId = employeeId;
        this.designation = designation;
        this.specialization = specialization;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
