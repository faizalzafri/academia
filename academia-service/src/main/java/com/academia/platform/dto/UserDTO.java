package com.academia.platform.dto;

public class UserDTO {
	private String userName;
	private String name;
	private String major;
	private String department;
	private String academicAdvior;
	private String email;
	private String description;

	public UserDTO(String userName, String name, String major, String department, String academicAdvior, String email,
			String description) {
		super();
		this.userName = userName;
		this.name = name;
		this.major = major;
		this.department = department;
		this.academicAdvior = academicAdvior;
		this.email = email;
		this.description = description;
	}

	public UserDTO() {
		super();
	}

	public UserDTO(String userName) {
		super();
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getAcademicAdvior() {
		return academicAdvior;
	}

	public void setAcademicAdvior(String academicAdvior) {
		this.academicAdvior = academicAdvior;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
