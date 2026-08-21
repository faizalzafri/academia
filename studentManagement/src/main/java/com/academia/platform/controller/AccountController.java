package com.academia.platform.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.academia.platform.model.Student;
import com.academia.platform.model.Teacher;
import com.academia.platform.model.User;
import com.academia.platform.dto.UserDTO;
import com.academia.platform.service.StudentService;
import com.academia.platform.service.UserService;

@Controller
public class AccountController {

	@Autowired
	private UserService userService;
	@Autowired
	private StudentService studentService;

	@GetMapping("/user/account")
	public String profile(Model model) {
		User user = userService.getUser();
		model.addAttribute("u", user);
		UserDTO dto = new UserDTO();
		dto.setUserName(user.getUsername());
		dto.setEmail(user.getEmail());
		dto.setDescription(user.getDescription());
		if (user instanceof Student) {
			Student s = (Student) user;
			dto.setName(s.getName());
			dto.setDepartment(s.getDepartment());
			dto.setMajor(s.getMajor());
			dto.setAcademicAdvior(s.getAcademicAdvior());
		} else if (user instanceof Teacher) {
			Teacher t = (Teacher) user;
			dto.setName(t.getName());
			dto.setDepartment(t.getDesignation());
		} else {
			dto.setName(user.getUsername());
			dto.setDepartment("Academic");
		}
		model.addAttribute("user", dto);
		return "profile";
	}

	@PostMapping({"/user/edit", "/user/account"})
	public String editUserAccount(@ModelAttribute("user") @Valid UserDTO userDTO,
			BindingResult result) {

		try {
			User user = userService.getUser();
			user.setDescription(userDTO.getDescription());
			if (user instanceof Student) {
				Student student = (Student) user;
				student.setDescription(userDTO.getDescription());
				studentService.saveOrUpdate(student);
			} else {
				userService.changeUserPassword(user, user.getPassword()); // or save user
			}
		} catch (Exception e) {
			// fallback
		}
		return "redirect:/user/account?success";
	}
}
