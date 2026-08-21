package com.academia.platform.controller;

import java.time.LocalDate;
import java.util.List;

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
import com.academia.platform.model.Activity;
import com.academia.platform.model.ActivityCategory;
import com.academia.platform.model.ActivityStatus;
import com.academia.platform.model.User;
import com.academia.platform.repository.TeacherRepository;
import com.academia.platform.service.AcademicYearService;
import com.academia.platform.service.ActivityService;
import com.academia.platform.service.UserService;

@Controller
@RequestMapping("/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherRepository teacherRepository;

    @GetMapping
    public String listActivities(@RequestParam(value = "yearId", required = false) Long yearId,
                                 @RequestParam(value = "category", required = false) ActivityCategory category,
                                 Model model) {
        AcademicYear activeYear = (yearId != null) 
                ? academicYearService.getAcademicYearById(yearId).orElse(academicYearService.getActiveAcademicYear())
                : academicYearService.getActiveAcademicYear();

        List<Activity> activities = (category != null) 
                ? activityService.getActivitiesByCategory(activeYear.getId(), category)
                : activityService.getActivitiesForYear(activeYear.getId());

        model.addAttribute("activities", activities);
        model.addAttribute("activeYear", activeYear);
        model.addAttribute("allYears", academicYearService.getAllAcademicYears());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", ActivityCategory.values());

        User currentUser = userService.getUser();
        model.addAttribute("currentUser", currentUser);

        return "activities_list";
    }

    @GetMapping("/{id}")
    public String activityDetail(@PathVariable("id") Long id, Model model) {
        Activity activity = activityService.getActivityById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + id));

        model.addAttribute("activity", activity);
        model.addAttribute("participants", activityService.getParticipants(id));
        model.addAttribute("results", activityService.getResults(id));

        User currentUser = userService.getUser();
        model.addAttribute("currentUser", currentUser);

        return "activity_detail";
    }

    @GetMapping("/manage")
    public String manageActivities(Model model) {
        AcademicYear activeYear = academicYearService.getActiveAcademicYear();
        model.addAttribute("activeYear", activeYear);
        model.addAttribute("activities", activityService.getActivitiesForYear(activeYear.getId()));
        model.addAttribute("categories", ActivityCategory.values());
        model.addAttribute("teachers", teacherRepository.findAll());
        return "activities_manage";
    }

    @PostMapping("/create")
    public String createActivity(@RequestParam("name") String name,
                                 @RequestParam("description") String description,
                                 @RequestParam("category") ActivityCategory category,
                                 @RequestParam(value = "yearId", required = false) Long yearId,
                                 @RequestParam("eventDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate eventDate,
                                 @RequestParam(value = "registrationDeadline", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate registrationDeadline,
                                 @RequestParam("venue") String venue,
                                 @RequestParam(value = "coordinatorUsername", required = false) String coordinatorUsername) {
        activityService.createActivity(name, description, category, yearId, eventDate, registrationDeadline, venue, coordinatorUsername);
        return "redirect:/activities/manage?created=true";
    }

    @PostMapping("/{id}/register")
    public String registerStudent(@PathVariable("id") Long id,
                                  @RequestParam(value = "studentUsername", required = false) String studentUsername,
                                  @RequestParam(value = "role", defaultValue = "Participant") String role,
                                  @RequestParam(value = "teamName", required = false) String teamName) {
        if (studentUsername == null || studentUsername.isEmpty()) {
            User current = userService.getUser();
            studentUsername = current.getUsername();
        }
        String result = activityService.registerStudent(id, studentUsername, role, teamName);
        return "redirect:/activities/" + id + "?result=" + result;
    }

    @PostMapping("/{id}/result")
    public String recordResult(@PathVariable("id") Long id,
                               @RequestParam("eventName") String eventName,
                               @RequestParam("winnerName") String winnerName,
                               @RequestParam(value = "winnerClass", required = false) String winnerClass,
                               @RequestParam("position") String position,
                               @RequestParam(value = "score", required = false) String score) {
        activityService.recordResult(id, eventName, winnerName, winnerClass, position, score);
        return "redirect:/activities/" + id + "?resultSaved=true";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") ActivityStatus status) {
        activityService.updateActivityStatus(id, status);
        return "redirect:/activities/" + id + "?statusUpdated=true";
    }

    @GetMapping("/student/{username}")
    public String studentActivities(@PathVariable("username") String username, Model model) {
        model.addAttribute("username", username);
        model.addAttribute("participations", activityService.getStudentActivities(username));
        return "student_activities";
    }
}
