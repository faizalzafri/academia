package com.academia.platform.controller;

import java.time.LocalDate;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.academia.platform.model.User;
import com.academia.platform.service.AcademicService;
import com.academia.platform.service.CourseService;
import com.academia.platform.service.SecurityService;
import com.academia.platform.service.UserService;

@Controller
public class HomeController {

	@Autowired
	private SecurityService securityService;
	@Autowired
	private MessageSource messages;
	@Autowired
	private UserService userService;
	@Autowired
	private AcademicService academicService;
	@Autowired
	private CourseService courseService;

	@GetMapping("/home")
	public String home(Model model) {
		try {
			User user = userService.getUser();
			if (user != null && user.getAuthorities() != null) {
				if (user.getAuthorities().contains("SYSTEM_ADMIN")) {
					return "redirect:/admin/dashboard";
				}
				if (user.getAuthorities().contains("PRINCIPAL")) {
					return "redirect:/principal/dashboard";
				}
				if (user.getAuthorities().contains("TEACHER")) {
					model.addAttribute("user", user);
					model.addAttribute("courses", courseService.getAll());
					model.addAttribute("events", academicService.getAllCalendarEvents());
					model.addAttribute("classSections", academicService.getAllClassSections());
					return "homepage";
				}
			}
			if (user != null) {
				model.addAttribute("user", user);
				model.addAttribute("courses", courseService.getAll());
				model.addAttribute("events", academicService.getAllCalendarEvents());
			}
		} catch (Exception e) {
			// fallback
		}
		return "homepagestudent";
	}

	@GetMapping("/forgotPassword")
	public String forgotPassword() {
		return "forgotPassword";
	}

	@GetMapping("/user/changePassword")
	public ModelAndView showChangePasswordPage(final ModelMap model, @RequestParam("token") final String token) {
		final String result = securityService.validatePasswordResetToken(token);
		if (result != null) {
			model.addAttribute("messageKey", "auth.message." + result);
			return new ModelAndView("redirect:/login", model);
		}
		model.addAttribute("token", token);
		return new ModelAndView("redirect:/updatePassword");
	}

	@GetMapping("/updatePassword")
	public ModelAndView updatePassword(final HttpServletRequest request, final ModelMap model,
			@RequestParam final Optional<String> messageKey) {
		model.addAttribute("lang", request.getLocale().getLanguage());
		messageKey.ifPresent(key ->
				model.addAttribute("message", messages.getMessage(key, null, request.getLocale())));
		return new ModelAndView("updatePassword", model);
	}
}
