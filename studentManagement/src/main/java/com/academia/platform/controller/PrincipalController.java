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
    public String yearReport(
            @PathVariable("id") Long id,
            @RequestParam(value = "classSectionId", required = false) Long classSectionId,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            Model model) {
        
        Map<String, Object> summary = academicYearService.getYearSummary(id);
        AcademicYear year = (AcademicYear) summary.get("year");
        List<StudentEnrollment> allEnrollments = studentEnrollmentRepository.findByAcademicYear(year);
        List<ClassSection> classSections = classSectionRepository.findByAcademicYear(year);
        if (classSections.isEmpty()) {
            classSections = classSectionRepository.findAll();
        }
        
        // Group enrollments by ClassSection ID
        Map<Long, List<StudentEnrollment>> enrollmentsBySection = allEnrollments.stream()
                .filter(e -> e.getClassSection() != null)
                .collect(java.util.stream.Collectors.groupingBy(e -> e.getClassSection().getId()));
        
        // Build Class-level summary rows
        List<Map<String, Object>> classSummaries = new java.util.ArrayList<>();
        for (ClassSection cs : classSections) {
            List<StudentEnrollment> sectionEnrollments = enrollmentsBySection.getOrDefault(cs.getId(), java.util.List.of());
            
            // Optional keyword filter within section
            List<StudentEnrollment> filteredSectionEnrollments = sectionEnrollments;
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                String term = searchKeyword.trim().toLowerCase();
                filteredSectionEnrollments = sectionEnrollments.stream()
                        .filter(e -> (e.getRollNumber() != null && e.getRollNumber().toLowerCase().contains(term))
                                || (e.getStudent() != null && e.getStudent().getName() != null && e.getStudent().getName().toLowerCase().contains(term))
                                || (e.getStudent() != null && e.getStudent().getUsername().toLowerCase().contains(term)))
                        .collect(java.util.stream.Collectors.toList());
            }

            long enrolledCount = sectionEnrollments.stream().filter(e -> e.getStatus() == com.academia.platform.model.EnrollmentStatus.ENROLLED).count();
            long promotedCount = sectionEnrollments.stream().filter(e -> e.getStatus() == com.academia.platform.model.EnrollmentStatus.PROMOTED).count();
            long graduatedCount = sectionEnrollments.stream().filter(e -> e.getStatus() == com.academia.platform.model.EnrollmentStatus.GRADUATED).count();
            
            Map<String, Object> cMap = new java.util.HashMap<>();
            cMap.put("classSection", cs);
            cMap.put("totalCount", sectionEnrollments.size());
            cMap.put("enrolledCount", enrolledCount);
            cMap.put("promotedCount", promotedCount);
            cMap.put("graduatedCount", graduatedCount);
            cMap.put("enrollments", filteredSectionEnrollments);
            classSummaries.add(cMap);
        }

        // If a specific classSectionId is selected, filter the displayed class summaries
        List<Map<String, Object>> displayedClassSummaries = classSummaries;
        if (classSectionId != null) {
            displayedClassSummaries = classSummaries.stream()
                    .filter(c -> ((ClassSection) c.get("classSection")).getId().equals(classSectionId))
                    .collect(java.util.stream.Collectors.toList());
        }

        model.addAttribute("summary", summary);
        model.addAttribute("classSummaries", displayedClassSummaries);
        model.addAttribute("allClassSections", classSections);
        model.addAttribute("selectedClassSectionId", classSectionId);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("enrollments", allEnrollments); // for backwards-compatibility
        
        return "principal_report";
    }

    @PostMapping("/promote/{fromYearId}/{toYearId}")
    public String promoteStudents(@PathVariable("fromYearId") Long fromYearId,
                                  @PathVariable("toYearId") Long toYearId) {
        int promoted = academicYearService.promoteStudents(fromYearId, toYearId);
        return "redirect:/principal/years/" + toYearId + "/report?promotedCount=" + promoted;
    }

    @GetMapping("/history")
    public String historicalBrowse(
            @RequestParam(value = "yearId", required = false) Long yearId,
            @RequestParam(value = "classSectionId", required = false) Long classSectionId,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            Model model) {
        
        List<AcademicYear> allYears = academicYearService.getAllAcademicYears();
        AcademicYear selectedYear = (yearId != null) 
                ? academicYearService.getAcademicYearById(yearId).orElse(academicYearService.getActiveAcademicYear())
                : academicYearService.getActiveAcademicYear();

        List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByAcademicYear(selectedYear);
        List<ClassSection> sections = classSectionRepository.findByAcademicYear(selectedYear);
        if (sections.isEmpty()) {
            sections = classSectionRepository.findAll();
        }

        // Filter enrollments by class section and keyword if provided
        List<StudentEnrollment> filteredEnrollments = enrollments.stream()
                .filter(e -> {
                    if (classSectionId != null && (e.getClassSection() == null || !classSectionId.equals(e.getClassSection().getId()))) {
                        return false;
                    }
                    if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                        String term = searchKeyword.trim().toLowerCase();
                        String roll = e.getRollNumber() != null ? e.getRollNumber().toLowerCase() : "";
                        String name = e.getStudent() != null && e.getStudent().getName() != null ? e.getStudent().getName().toLowerCase() : "";
                        String username = e.getStudent() != null ? e.getStudent().getUsername().toLowerCase() : "";
                        return roll.contains(term) || name.contains(term) || username.contains(term);
                    }
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("allYears", allYears);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("enrollments", filteredEnrollments);
        model.addAttribute("totalEnrollmentsCount", enrollments.size());
        model.addAttribute("sections", sections);
        model.addAttribute("selectedClassSectionId", classSectionId);
        model.addAttribute("searchKeyword", searchKeyword);

        return "principal_history";
    }
}
