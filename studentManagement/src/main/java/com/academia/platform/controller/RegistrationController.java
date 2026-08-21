package com.academia.platform.controller;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.academia.platform.dto.RegistrationRequestDTO;
import com.academia.platform.model.User;
import com.academia.platform.service.UserService;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String showRegistration(Model model) {
        if (!model.containsAttribute("registration")) {
            model.addAttribute("registration", new RegistrationRequestDTO());
        }
        return "register";
    }

    @PostMapping
    public String registerUserAccount(
            @ModelAttribute("registration") @Valid RegistrationRequestDTO registrationDTO,
            BindingResult result,
            Model model) {

        // Validate password confirmation
        if (registrationDTO.getPassword() != null && !registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.registration", "Passwords do not match");
        }

        // Check if username already exists
        Optional<User> existingUsername = userService.findById(registrationDTO.getUsername());
        if (existingUsername.isPresent()) {
            result.rejectValue("username", "error.registration", "Username is already taken");
        }

        // Check if email already exists
        User existingEmail = userService.getUserByEmail(registrationDTO.getEmail());
        if (existingEmail != null) {
            result.rejectValue("email", "error.registration", "Email address is already in use");
        }

        if (result.hasErrors()) {
            return "register";
        }

        userService.registerUser(registrationDTO);
        return "redirect:/login?pendingApproval=true";
    }
}
