package com.academia.platform.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.academia.platform.service.UserService;

@Controller
public class MainController {

	@Autowired
	private UserService userService;

	@GetMapping({"/login", "/"})
	public String login(Model model, String error, String logout) {
		if (error != null)
			model.addAttribute("error", "Your username and password is invalid.");
		if (logout != null)
			model.addAttribute("message", "You have been logged out successfully.");
		return "login";
	}

	@GetMapping("/contact")
	public String contact() {
		return "page-contact";
	}

	@GetMapping("/file")
	public String file() {
		return "file";
	}

	@RequestMapping("/becomeTeacher")
	public String becomeTeacher() {
		userService.becomeTeacher();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			List<GrantedAuthority> updatedAuthorities = new ArrayList<>(auth.getAuthorities());
			if (!updatedAuthorities.stream().anyMatch(a -> a.getAuthority().equals("TEACHER"))) {
				updatedAuthorities.add(new SimpleGrantedAuthority("TEACHER"));
			}
			Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);
			SecurityContextHolder.getContext().setAuthentication(newAuth);
		}
		return "redirect:/home";
	}

	@GetMapping("/student/home")
	public String studentHome() {
		return "homepagestudent";
	}
}
