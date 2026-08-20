package edu.hanu.studentManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.hanu.studentManagement.model.ClassSection;
import edu.hanu.studentManagement.model.DayOfWeek;
import edu.hanu.studentManagement.model.SchoolCalendarEvent;
import edu.hanu.studentManagement.model.EventType;
import edu.hanu.studentManagement.service.CbseService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Controller
@RequestMapping("/cbse")
public class CbseController {

    @Autowired
    private CbseService cbseService;

    // Directs to main CBSE management dashboard
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("classSections", cbseService.getAllClassSections());
        model.addAttribute("teachers", cbseService.getAllTeachers());
        model.addAttribute("students", cbseService.getAllStudents());
        return "cbse_dashboard";
    }

    // Handles assignment of teacher and captains to a class section
    @PostMapping("/assign")
    public String assignClassRoles(
            @RequestParam("classSectionId") Long classSectionId,
            @RequestParam(value = "teacherUsername", required = false) String teacherUsername,
            @RequestParam(value = "captainUsername", required = false) String captainUsername,
            @RequestParam(value = "sportsCaptainUsername", required = false) String sportsCaptainUsername) {
        
        cbseService.assignTeacherAndCaptains(classSectionId, teacherUsername, captainUsername, sportsCaptainUsername);
        return "redirect:/cbse/dashboard?assignSuccess=true";
    }

    // Directs to timetable management page for a specific class section
    @GetMapping("/timetable/{id}")
    public String showTimetable(@PathVariable("id") Long classSectionId, Model model) {
        ClassSection cs = cbseService.getClassSectionById(classSectionId).orElse(null);
        if (cs == null) {
            return "redirect:/cbse/dashboard";
        }
        model.addAttribute("classSection", cs);
        model.addAttribute("slots", cbseService.getTimetableForClassSection(classSectionId));
        model.addAttribute("subjects", cbseService.getAllSubjects());
        model.addAttribute("teachers", cbseService.getAllTeachers());
        model.addAttribute("days", DayOfWeek.values());
        return "cbse_timetable";
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

        String result = cbseService.addTimetableSlot(classSectionId, subjectId, teacherUsername, dayOfWeek, periodNumber, startTime, endTime);
        if ("success".equals(result)) {
            return "redirect:/cbse/timetable/" + classSectionId + "?success=true";
        } else {
            return "redirect:/cbse/timetable/" + classSectionId + "?error=" + result;
        }
    }

    // Deletes a slot from class timetable
    @GetMapping("/timetable/delete/{id}")
    public String deleteTimetableSlot(
            @PathVariable("id") Long slotId,
            @RequestParam("classSectionId") Long classSectionId) {
        cbseService.deleteTimetableSlot(slotId);
        return "redirect:/cbse/timetable/" + classSectionId + "?deleteSuccess=true";
    }

    // Directs to yearly calendar management view
    @GetMapping("/calendar")
    public String showCalendar(Model model) {
        model.addAttribute("events", cbseService.getAllCalendarEvents());
        model.addAttribute("eventTypes", EventType.values());
        return "cbse_calendar";
    }

    // Handles addition of new calendar event / holiday
    @PostMapping("/calendar/add")
    public String addCalendarEvent(
            @RequestParam("eventDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("type") EventType type) {
        cbseService.addCalendarEvent(new SchoolCalendarEvent(eventDate, title, description, type));
        return "redirect:/cbse/calendar?success=true";
    }

    // Deletes an event from school calendar
    @GetMapping("/calendar/delete/{id}")
    public String deleteCalendarEvent(@PathVariable("id") Long id) {
        cbseService.deleteCalendarEvent(id);
        return "redirect:/cbse/calendar?deleteSuccess=true";
    }
}
