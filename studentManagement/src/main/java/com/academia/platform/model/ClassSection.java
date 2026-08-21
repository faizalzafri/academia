package com.academia.platform.model;

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
    name = "class_sections",
    uniqueConstraints = @UniqueConstraint(columnNames = {"class_id", "section_id", "academic_year_id"})
)
public class ClassSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @ManyToOne
    @JoinColumn(name = "class_teacher_username")
    private Teacher classTeacher;

    @ManyToOne
    @JoinColumn(name = "class_captain_username")
    private Student classCaptain;

    @ManyToOne
    @JoinColumn(name = "sports_captain_username")
    private Student sportsCaptain;

    public ClassSection() {
    }

    public ClassSection(SchoolClass schoolClass, Section section) {
        this.schoolClass = schoolClass;
        this.section = section;
    }

    public ClassSection(SchoolClass schoolClass, Section section, AcademicYear academicYear) {
        this.schoolClass = schoolClass;
        this.section = section;
        this.academicYear = academicYear;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SchoolClass getSchoolClass() {
        return schoolClass;
    }

    public void setSchoolClass(SchoolClass schoolClass) {
        this.schoolClass = schoolClass;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYear academicYear) {
        this.academicYear = academicYear;
    }

    public Teacher getClassTeacher() {
        return classTeacher;
    }

    public void setClassTeacher(Teacher classTeacher) {
        this.classTeacher = classTeacher;
    }

    public Student getClassCaptain() {
        return classCaptain;
    }

    public void setClassCaptain(Student classCaptain) {
        this.classCaptain = classCaptain;
    }

    public Student getSportsCaptain() {
        return sportsCaptain;
    }

    public void setSportsCaptain(Student sportsCaptain) {
        this.sportsCaptain = sportsCaptain;
    }

    // Helper to get formatted name e.g. "Class 10 - A"
    public String getDisplayName() {
        return (schoolClass != null ? schoolClass.getClassName() : "") + " - " + (section != null ? section.getSectionName() : "");
    }
}
