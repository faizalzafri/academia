package com.academia.platform.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.academia.platform.model.Course;
import com.academia.platform.dto.CourseDTO;
import com.academia.platform.service.CourseService;
import com.academia.platform.service.FileService;

@Controller
public class CourseController {
	
	@Autowired
	private CourseService courseService;
	@Autowired
	private FileService fileService;
	
	@GetMapping("/teacher/course/{id}")
	public String teacherCourse(@PathVariable("id") long id, Model model) {
		model.addAttribute("course", courseService.getById(id));
		model.addAttribute("files", fileService.getAll());
		return "teacher-course";
	}
	
	@GetMapping("/manage/course")
	public String manageCourse(Model model) {
		List<Course> courses = courseService.getAll();
		model.addAttribute("courses", courses);
		model.addAttribute("course", new CourseDTO());
		return "manage-course";
	}
	
	@GetMapping("/listCourse")
	public String listCourse(Model model) {
		model.addAttribute("courses", courseService.getAll());
		return "list-course";
	}
	
	@PostMapping("/addCourse")
    public String createCourse(@ModelAttribute("course") @Valid CourseDTO courseDTO,
        BindingResult result) {
		Course course = new Course();
		course.setName(courseDTO.getName());
		course.setCode(courseDTO.getCode());
		course.setCredit(courseDTO.getCredit());
		course.setDescription(courseDTO.getDescription());

        courseService.save(course);
        return "redirect:/manage/course?success";
    }
	
	@GetMapping("/deleteCourse/{id}")
	public String deleteCourse(@PathVariable("id") long id) {
		courseService.delete(id);
		return "redirect:/manage/course?success";
	}
	
	@GetMapping("/enroll/{id}")
	public String enrollCourse(@PathVariable("id") long id) {
		courseService.enrollCourse(id);
		return "redirect:/listCourse?success";
	}
	

	@PostMapping("/updateCourse/{id}")
    public String updateCourse(@RequestParam(value="description", defaultValue="") String description,
                                @RequestParam(value="references", defaultValue="") String references,
                                @PathVariable("id") long id) {
        CourseDTO dto = new CourseDTO();
        dto.setDescription(description);
        dto.setReferences(references);
        courseService.updateCourse(dto, id);
        return "redirect:/teacher/course/"+id+"?success";
    }
	
	
	@PostMapping("/teacher/course/upload")
	public String uploadCourseFile(@RequestParam("file") MultipartFile file,
	                               @RequestParam("courseId") long courseId) {
		fileService.storeFile(file);
		return "redirect:/teacher/course/" + courseId + "?uploadSuccess=true";
	}

	@GetMapping("/student/course/{id}")
	public String studentCourse(@PathVariable("id") long id, Model model) {
		model.addAttribute("course", courseService.getById(id));
		model.addAttribute("files", fileService.getAll());
		return "student-course";
	}
}
