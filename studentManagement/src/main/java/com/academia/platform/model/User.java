package com.academia.platform.model;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;

@Entity(name = "users")
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@jakarta.persistence.DiscriminatorValue("user")
@Access(AccessType.FIELD)
public class User {
	@Id
	@Column(length = 20)
	private String username;
	private String password;
	private boolean enabled;
	@Column(length = 100)
	@Email
	private String email;
	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "authorities")
	private Set<String> authorities;
	@OneToMany(mappedBy = "users")
	private Set<File> files;
	@ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
        name = "user_course", 
        joinColumns = { @JoinColumn(name = "username") }, 
        inverseJoinColumns = { @JoinColumn(name = "course_id") }
    )
	private Set<Course> course;
	@Column(length = 4000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "approval_status", length = 20)
	private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

	@Column(name = "registration_date")
	private LocalDateTime registrationDate;

	public User() {
	}

	public User(String email, String username, String password, boolean enabled,
			Set<String> authorities, String description) {
		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.authorities = authorities;
		this.description = description;
		this.email = email;
		this.approvalStatus = ApprovalStatus.APPROVED;
		this.registrationDate = LocalDateTime.now();
	}

	public Set<String> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Set<String> authorities) {
		this.authorities = authorities;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Set<File> getFiles() {
		return files;
	}

	public void setFiles(Set<File> files) {
		this.files = files;
	}

	public Set<Course> getCourse() {
		return course;
	}

	public void setCourse(Set<Course> course) {
		this.course = course;
	}

	public ApprovalStatus getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(ApprovalStatus approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public LocalDateTime getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(LocalDateTime registrationDate) {
		this.registrationDate = registrationDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		User other = (User) obj;
		if (username == null) {
			return other.username == null;
		}
		return username.equals(other.username);
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", enabled=" + enabled + ", approvalStatus=" + approvalStatus + ", authorities=" + authorities + "]";
	}
}
