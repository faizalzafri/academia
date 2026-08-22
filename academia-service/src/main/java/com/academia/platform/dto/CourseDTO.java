package com.academia.platform.dto;

public class CourseDTO {
	private String name;
	private String code;
	private Integer credit;
	private String description;
	private String references;
	
	public CourseDTO(String description, String references) {
		super();
		this.description = description;
		this.references = references;
	}

	public CourseDTO() {
		super();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReferences() {
		return references;
	}

	public void setReferences(String references) {
		this.references = references;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getCredit() {
		return credit;
	}

	public void setCredit(Integer credit) {
		this.credit = credit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
