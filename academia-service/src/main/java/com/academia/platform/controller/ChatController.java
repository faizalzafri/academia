package com.academia.platform.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.academia.platform.service.UserService;

@Controller
public class ChatController {
	
	@Autowired
	private UserService userService;
	
	@RequestMapping("/chat")
    public String index(HttpServletRequest request, Model model) {
        model.addAttribute("username", userService.getUser().getUsername());
 
        return "chat";
    }
}
