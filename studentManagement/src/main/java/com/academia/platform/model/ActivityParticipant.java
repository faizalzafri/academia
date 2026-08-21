package com.academia.platform.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
    name = "activity_participants",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "student_username"})
)
public class ActivityParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne
    @JoinColumn(name = "student_username", nullable = false)
    private Student student;

    @Column(length = 50)
    private String role = "Participant"; // e.g. "Participant", "Captain", "Volunteer", "Performer"

    @Column(length = 100)
    private String teamName; // e.g. "Red House", "Class 10-A Team", "Pom Dance Troupe"

    @Column(length = 50)
    private String position; // e.g. "1st Place", "Gold Medal", "Participant"

    @Column(length = 250)
    private String remarks;

    private LocalDateTime registeredAt = LocalDateTime.now();

    public ActivityParticipant() {
    }

    public ActivityParticipant(Activity activity, Student student, String role, String teamName) {
        this.activity = activity;
        this.student = student;
        this.role = role;
        this.teamName = teamName;
        this.registeredAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
