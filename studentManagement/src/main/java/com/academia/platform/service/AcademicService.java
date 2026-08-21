package com.academia.platform.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academia.platform.model.AcademicYear;
import com.academia.platform.model.ClassSection;
import com.academia.platform.model.SchoolClass;
import com.academia.platform.model.Section;
import com.academia.platform.model.Student;
import com.academia.platform.model.Subject;
import com.academia.platform.model.Teacher;
import com.academia.platform.repository.ClassSectionRepository;
import com.academia.platform.repository.SchoolClassRepository;
import com.academia.platform.repository.SectionRepository;
import com.academia.platform.repository.StudentRepository;
import com.academia.platform.repository.SubjectRepository;
import com.academia.platform.repository.TeacherRepository;
import com.academia.platform.model.DayOfWeek;
import com.academia.platform.model.TimetableSlot;
import com.academia.platform.repository.TimetableSlotRepository;
import com.academia.platform.model.SchoolCalendarEvent;
import com.academia.platform.model.EventType;
import com.academia.platform.repository.SchoolCalendarEventRepository;

@Service
@Transactional
public class AcademicService {

    @Autowired
    private SchoolClassRepository schoolClassRepository;

    @Autowired
    private TimetableSlotRepository timetableSlotRepository;

    @Autowired
    private SchoolCalendarEventRepository schoolCalendarEventRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AcademicYearService academicYearService;

    // Seeds initial database on application startup
    @EventListener(ApplicationReadyEvent.class)
    public void seedInitialData() {
        AcademicYear activeYear = academicYearService.getActiveAcademicYear();

        // Seed Classes 1 to 12
        if (schoolClassRepository.count() == 0) {
            for (int i = 1; i <= 12; i++) {
                schoolClassRepository.save(new SchoolClass("Class " + i));
            }
        }

        // Seed Sections A, B, C
        if (sectionRepository.count() == 0) {
            sectionRepository.save(new Section("A"));
            sectionRepository.save(new Section("B"));
            sectionRepository.save(new Section("C"));
        }

        // Seed Core Subjects
        if (subjectRepository.count() == 0) {
            subjectRepository.save(new Subject("Mathematics", "MATH-10"));
            subjectRepository.save(new Subject("Science", "SCI-10"));
            subjectRepository.save(new Subject("English", "ENG-10"));
            subjectRepository.save(new Subject("Hindi", "HIN-10"));
            subjectRepository.save(new Subject("Social Science", "SOC-10"));
        }

        // Seed Class-Section combinations
        if (classSectionRepository.count() == 0) {
            List<SchoolClass> classes = schoolClassRepository.findAll();
            List<Section> sections = sectionRepository.findAll();
            for (SchoolClass c : classes) {
                for (Section s : sections) {
                    classSectionRepository.save(new ClassSection(c, s, activeYear));
                }
            }
        }
    }

    public List<ClassSection> getAllClassSections() {
        return classSectionRepository.findAll();
    }

    public Optional<ClassSection> getClassSectionById(Long id) {
        return classSectionRepository.findById(id);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // Assigns class teacher and captains to a class section
    public ClassSection assignTeacherAndCaptains(Long classSectionId, String teacherUsername, String captainUsername, String sportsCaptainUsername) {
        Optional<ClassSection> oCs = classSectionRepository.findById(classSectionId);
        if (oCs.isPresent()) {
            ClassSection cs = oCs.get();

            if (teacherUsername != null && !teacherUsername.isEmpty()) {
                Teacher teacher = teacherRepository.findById(teacherUsername).orElse(null);
                cs.setClassTeacher(teacher);
            } else {
                cs.setClassTeacher(null);
            }

            if (captainUsername != null && !captainUsername.isEmpty()) {
                Student captain = studentRepository.findById(captainUsername).orElse(null);
                cs.setClassCaptain(captain);
            } else {
                cs.setClassCaptain(null);
            }

            if (sportsCaptainUsername != null && !sportsCaptainUsername.isEmpty()) {
                Student sportsCaptain = studentRepository.findById(sportsCaptainUsername).orElse(null);
                cs.setSportsCaptain(sportsCaptain);
            } else {
                cs.setSportsCaptain(null);
            }

            return classSectionRepository.save(cs);
        }
        return null;
    }

    public List<TimetableSlot> getTimetableForClassSection(Long classSectionId) {
        ClassSection cs = classSectionRepository.findById(classSectionId).orElse(null);
        return cs != null ? timetableSlotRepository.findByClassSection(cs) : List.of();
    }

    public List<TimetableSlot> getTimetableForTeacher(String teacherUsername) {
        Teacher teacher = teacherRepository.findById(teacherUsername).orElse(null);
        return teacher != null ? timetableSlotRepository.findByTeacher(teacher) : List.of();
    }

    public void deleteTimetableSlot(Long slotId) {
        timetableSlotRepository.deleteById(slotId);
    }

    // Tries to add a timetable slot, returns error message if conflict exists, otherwise "success"
    public String addTimetableSlot(Long classSectionId, Long subjectId, String teacherUsername, DayOfWeek day, int period, String start, String end) {
        ClassSection classSection = classSectionRepository.findById(classSectionId).orElse(null);
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        Teacher teacher = teacherRepository.findById(teacherUsername).orElse(null);

        if (classSection == null || subject == null || teacher == null) {
            return "Invalid data provided.";
        }

        // Check if teacher is busy
        List<TimetableSlot> teacherSlots = timetableSlotRepository.findByTeacherAndDayOfWeekAndPeriodNumber(teacher, day, period);
        if (!teacherSlots.isEmpty()) {
            return "Conflict: Teacher " + teacher.getName() + " is already assigned to " 
                + teacherSlots.get(0).getClassSection().getDisplayName() + " on " + day + " period " + period + ".";
        }

        // Check if classroom (ClassSection) is busy
        List<TimetableSlot> classSlots = timetableSlotRepository.findByClassSectionAndDayOfWeekAndPeriodNumber(classSection, day, period);
        if (!classSlots.isEmpty()) {
            return "Conflict: " + classSection.getDisplayName() + " already has " 
                + classSlots.get(0).getSubject().getName() + " scheduled on " + day + " period " + period + ".";
        }

        // Save new slot
        TimetableSlot slot = new TimetableSlot(classSection, subject, teacher, day, period, start, end);
        slot.setAcademicYear(classSection.getAcademicYear() != null ? classSection.getAcademicYear() : academicYearService.getActiveAcademicYear());
        timetableSlotRepository.save(slot);
        return "success";
    }

    public List<SchoolCalendarEvent> getAllCalendarEvents() {
        return schoolCalendarEventRepository.findAllByOrderByEventDateAsc();
    }

    public SchoolCalendarEvent addCalendarEvent(SchoolCalendarEvent event) {
        if (event.getAcademicYear() == null) {
            event.setAcademicYear(academicYearService.getActiveAcademicYear());
        }
        return schoolCalendarEventRepository.save(event);
    }

    public void deleteCalendarEvent(Long id) {
        schoolCalendarEventRepository.deleteById(id);
    }
}
