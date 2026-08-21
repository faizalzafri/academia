package com.academia.platform.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity(name = "student")
@DiscriminatorValue("student")
public class Student extends User {
	@Column(name = "student_name", length = 30)
	private String name;
	@Enumerated(EnumType.STRING)
	private Gender gender;
	@Column(name = "date_of_birth", length = 50)
	private String dateOfBirth;
	@Column(length = 30)
	private String major;
	@Column(length = 30)
	private String department;
	@Column(length = 30)
	private String cohort;
	@Column(name = "academic_advior", length = 50)
	private String academicAdvior;

	public Student() {
		super();
	}

	public Student(String id, String name, Gender gender, String dateOfBirth, String major, String department,
			String cohort, String academicAdvior, String userName, String email, String password, boolean enabled,
			Set<String> authorities, String description) {
		super(email, id, password, enabled, authorities, description);
		this.name = name;
		this.gender = gender;
		this.dateOfBirth = dateOfBirth;
		this.major = major;
		this.department = department;
		this.cohort = cohort;
		this.academicAdvior = academicAdvior;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getDateOfBirth() {
		return this.dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getCohort() {
		return cohort;
	}

	public void setCohort(String cohort) {
		this.cohort = cohort;
	}

	public String getAcademicAdvior() {
		return academicAdvior;
	}

	public void setAcademicAdvior(String academicAdvior) {
		this.academicAdvior = academicAdvior;
	}
}
