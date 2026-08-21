package com.academia.platform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.academia.platform.model.User;
import com.academia.platform.service.AcademicService;
import com.academia.platform.service.CourseService;
import com.academia.platform.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AcademicService academicService;

    @Autowired
    private CourseService courseService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<User> pendingUsers = userService.getPendingUsers();
        model.addAttribute("pendingUsers", pendingUsers);
        model.addAttribute("pendingCount", pendingUsers.size());
        model.addAttribute("totalUsers", userService.getAll().size());
        model.addAttribute("totalCourses", courseService.getAll().size());
        model.addAttribute("totalClasses", academicService.getAllClassSections().size());
        return "admin_dashboard";
    }

    @GetMapping("/approvals")
    public String viewApprovals(Model model) {
        List<User> pendingUsers = userService.getPendingUsers();
        model.addAttribute("pendingUsers", pendingUsers);
        return "admin_approvals";
    }

    @PostMapping("/approve/{username}")
    public String approveUser(@PathVariable("username") String username, RedirectAttributes redirectAttributes) {
        try {
            userService.approveUser(username);
            redirectAttributes.addFlashAttribute("successMessage", "Account for " + username + " has been approved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving user: " + e.getMessage());
        }
        return "redirect:/admin/approvals";
    }

    @PostMapping("/reject/{username}")
    public String rejectUser(@PathVariable("username") String username, RedirectAttributes redirectAttributes) {
        try {
            userService.rejectUser(username);
            redirectAttributes.addFlashAttribute("infoMessage", "Account for " + username + " has been rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting user: " + e.getMessage());
        }
        return "redirect:/admin/approvals";
    }
}
