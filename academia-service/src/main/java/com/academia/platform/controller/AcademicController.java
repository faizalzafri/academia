package com.academia.platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.academia.platform.model.ClassSection;
import com.academia.platform.model.DayOfWeek;
import com.academia.platform.model.SchoolCalendarEvent;
import com.academia.platform.model.EventType;
import com.academia.platform.service.AcademicService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Controller
@RequestMapping("/academic")
public class AcademicController {

    @Autowired
    private AcademicService academicService;

    // Directs to main academic management dashboard with search and cohort filters
    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam(value = "classId", required = false) Long classId,
            @RequestParam(value = "sectionId", required = false) Long sectionId,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {
        
        java.util.List<ClassSection> filteredSections = academicService.searchClassSections(classId, sectionId, keyword);
        model.addAttribute("classSections", filteredSections);
        model.addAttribute("allClasses", academicService.getAllClasses());
        model.addAttribute("allSections", academicService.getAllSections());
        model.addAttribute("teachers", academicService.getAllTeachers());
        model.addAttribute("students", academicService.getAllStudents()); // general list fallback
        model.addAttribute("subjects", academicService.getAllSubjects());
        model.addAttribute("selectedClassId", classId);
        model.addAttribute("selectedSectionId", sectionId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("studentsBySection", academicService.getStudentsMapForClassSections(filteredSections));
        model.addAttribute("totalClassCount", academicService.getAllClassSections().size());
        
        return "academic_dashboard";
    }

    // Handles assignment of teacher and captains to a class section
    @PostMapping("/assign")
    public String assignClassRoles(
            @RequestParam("classSectionId") Long classSectionId,
            @RequestParam(value = "teacherUsername", required = false) String teacherUsername,
            @RequestParam(value = "captainUsername", required = false) String captainUsername,
            @RequestParam(value = "sportsCaptainUsername", required = false) String sportsCaptainUsername) {
        
        academicService.assignTeacherAndCaptains(classSectionId, teacherUsername, captainUsername, sportsCaptainUsername);
        return "redirect:/academic/dashboard?assignSuccess=true";
    }

    // Directs to timetable management page for a specific class section
    @GetMapping("/timetable/{id}")
    public String showTimetable(@PathVariable("id") Long classSectionId, Model model) {
        ClassSection cs = academicService.getClassSectionById(classSectionId).orElse(null);
        if (cs == null) {
            return "redirect:/academic/dashboard";
        }
        model.addAttribute("classSection", cs);
        model.addAttribute("slots", academicService.getTimetableForClassSection(classSectionId));
        model.addAttribute("subjects", academicService.getAllSubjects());
        model.addAttribute("teachers", academicService.getAllTeachers());
        model.addAttribute("days", DayOfWeek.values());
        return "academic_timetable";
    }

    // Adds a slot in the timetable with conflict detection
    @PostMapping("/timetable/add")
    public String addTimetableSlot(
            @RequestParam("classSectionId") Long classSectionId,
            @RequestParam("subjectId") Long subjectId,
            @RequestParam("teacherUsername") String teacherUsername,
            @RequestParam("dayOfWeek") DayOfWeek dayOfWeek,
            @RequestParam("periodNumber") int periodNumber,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime) {

        String result = academicService.addTimetableSlot(classSectionId, subjectId, teacherUsername, dayOfWeek, periodNumber, startTime, endTime);
        if ("success".equals(result)) {
            return "redirect:/academic/timetable/" + classSectionId + "?success=true";
        } else {
            return "redirect:/academic/timetable/" + classSectionId + "?error=" + result;
        }
    }

    // Deletes a slot from class timetable
    @GetMapping("/timetable/delete/{id}")
    public String deleteTimetableSlot(
            @PathVariable("id") Long slotId,
            @RequestParam("classSectionId") Long classSectionId) {
        academicService.deleteTimetableSlot(slotId);
        return "redirect:/academic/timetable/" + classSectionId + "?deleteSuccess=true";
    }

    // Directs to yearly calendar management view
    @GetMapping("/calendar")
    public String showCalendar(Model model) {
        model.addAttribute("events", academicService.getAllCalendarEvents());
        model.addAttribute("eventTypes", EventType.values());
        return "academic_calendar";
    }

    // Handles addition of new calendar event / holiday
    @PostMapping("/calendar/add")
    public String addCalendarEvent(
            @RequestParam("eventDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("type") EventType type) {
        academicService.addCalendarEvent(new SchoolCalendarEvent(eventDate, title, description, type));
        return "redirect:/academic/calendar?success=true";
    }

    // Deletes an event from school calendar
    @GetMapping("/calendar/delete/{id}")
    public String deleteCalendarEvent(@PathVariable("id") Long id) {
        academicService.deleteCalendarEvent(id);
        return "redirect:/academic/calendar?deleteSuccess=true";
    }
}
