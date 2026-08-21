package com.academia.platform.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.academia.platform.model.AcademicYear;
import com.academia.platform.model.ClassSection;
import com.academia.platform.model.StudentEnrollment;
import com.academia.platform.model.User;
import com.academia.platform.repository.ClassSectionRepository;
import com.academia.platform.repository.StudentEnrollmentRepository;
import com.academia.platform.repository.TeacherRepository;
import com.academia.platform.service.AcademicYearService;
import com.academia.platform.service.ActivityService;
import com.academia.platform.service.UserService;

@Controller
@RequestMapping("/principal")
public class PrincipalController {

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ClassSectionRepository classSectionRepository;

    @Autowired
    private StudentEnrollmentRepository studentEnrollmentRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        AcademicYear activeYear = academicYearService.getActiveAcademicYear();
        List<AcademicYear> allYears = academicYearService.getAllAcademicYears();

        model.addAttribute("activeYear", activeYear);
        model.addAttribute("allYears", allYears);
        model.addAttribute("totalTeachers", teacherRepository.count());
        model.addAttribute("totalStudents", studentEnrollmentRepository.countByAcademicYear(activeYear));
        model.addAttribute("totalActivities", activityService.getActivitiesForYear(activeYear.getId()).size());
        model.addAttribute("classSections", classSectionRepository.findAll());

        return "principal_dashboard";
    }

    @GetMapping("/years")
    public String listYears(Model model) {
        model.addAttribute("years", academicYearService.getAllAcademicYears());
        model.addAttribute("activeYear", academicYearService.getActiveAcademicYear());
        return "principal_years";
    }

    @PostMapping("/years/create")
    public String createYear(@RequestParam("name") String name,
                             @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                             @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        User creator = userService.getUser();
        academicYearService.createAcademicYear(name, startDate, endDate, creator);
        return "redirect:/principal/years?created=true";
    }

    @PostMapping("/years/{id}/activate")
    public String activateYear(@PathVariable("id") Long id) {
        academicYearService.activateAcademicYear(id);
        return "redirect:/principal/years?activated=true";
    }

    @PostMapping("/years/{id}/complete")
    public String completeYear(@PathVariable("id") Long id) {
        academicYearService.completeAcademicYear(id);
        return "redirect:/principal/years?completed=true";
    }

    @GetMapping("/years/{id}/report")
    public String yearReport(@PathVariable("id") Long id, Model model) {
        Map<String, Object> summary = academicYearService.getYearSummary(id);
        model.addAttribute("summary", summary);
        model.addAttribute("enrollments", studentEnrollmentRepository.findByAcademicYear((AcademicYear) summary.get("year")));
        return "principal_report";
    }

    @PostMapping("/promote/{fromYearId}/{toYearId}")
    public String promoteStudents(@PathVariable("fromYearId") Long fromYearId,
                                  @PathVariable("toYearId") Long toYearId) {
        int promoted = academicYearService.promoteStudents(fromYearId, toYearId);
        return "redirect:/principal/years/" + toYearId + "/report?promotedCount=" + promoted;
    }

    @GetMapping("/history")
    public String historicalBrowse(@RequestParam(value = "yearId", required = false) Long yearId, Model model) {
        List<AcademicYear> allYears = academicYearService.getAllAcademicYears();
        AcademicYear selectedYear = (yearId != null) 
                ? academicYearService.getAcademicYearById(yearId).orElse(academicYearService.getActiveAcademicYear())
                : academicYearService.getActiveAcademicYear();

        List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByAcademicYear(selectedYear);
        List<ClassSection> sections = classSectionRepository.findAll();

        model.addAttribute("allYears", allYears);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("sections", sections);

        return "principal_history";
    }
}
