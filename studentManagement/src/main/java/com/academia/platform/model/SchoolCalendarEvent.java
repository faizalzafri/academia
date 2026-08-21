package com.academia.platform.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "school_calendar_events")
public class SchoolCalendarEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDate;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 250)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventType type;

    @ManyToOne
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    public SchoolCalendarEvent() {
    }

    public SchoolCalendarEvent(LocalDate eventDate, String title, String description, EventType type) {
        this.eventDate = eventDate;
        this.title = title;
        this.description = description;
        this.type = type;
    }

    public SchoolCalendarEvent(LocalDate eventDate, String title, String description, EventType type, AcademicYear academicYear) {
        this.eventDate = eventDate;
        this.title = title;
        this.description = description;
        this.type = type;
        this.academicYear = academicYear;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYear academicYear) {
        this.academicYear = academicYear;
    }
}
