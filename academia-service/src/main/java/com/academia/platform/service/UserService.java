package com.academia.platform.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academia.platform.dto.RegistrationRequestDTO;
import com.academia.platform.model.ApprovalStatus;
import com.academia.platform.model.PasswordResetToken;
import com.academia.platform.model.Student;
import com.academia.platform.model.Teacher;
import com.academia.platform.model.User;
import com.academia.platform.repository.PasswordResetTokenRepository;
import com.academia.platform.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordResetTokenRepository passwordResetTokenRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	public Optional<User> findById(String username) {
		return userRepository.findById(username);
	}

	public User getUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return null;
		}
		Object principal = auth.getPrincipal();
		if (principal instanceof UserDetails) {
			String username = ((UserDetails) principal).getUsername();
			return userRepository.findById(username).orElse(null);
		}
		return null;
	}

	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email).orElse(null);
	}

	@Transactional
	public User registerUser(RegistrationRequestDTO dto) {
		if ("TEACHER".equalsIgnoreCase(dto.getRole())) {
			Teacher teacher = new Teacher();
			teacher.setUsername(dto.getUsername());
			teacher.setEmail(dto.getEmail());
			teacher.setPassword(passwordEncoder.encode(dto.getPassword()));
			teacher.setName(dto.getName());
			teacher.setEmployeeId(dto.getEmployeeId());
			teacher.setDesignation(dto.getDesignation());
			teacher.setDepartment(dto.getDepartment());
			teacher.setSpecialization(dto.getSpecialization());
			teacher.setDescription(dto.getDescription());
			teacher.setEnabled(false);
			teacher.setApprovalStatus(ApprovalStatus.PENDING);
			teacher.setRegistrationDate(LocalDateTime.now());
			
			Set<String> authorities = new HashSet<>();
			authorities.add("TEACHER");
			authorities.add("USER");
			teacher.setAuthorities(authorities);
			return userRepository.save(teacher);
		} else {
			Student student = new Student();
			student.setUsername(dto.getUsername());
			student.setEmail(dto.getEmail());
			student.setPassword(passwordEncoder.encode(dto.getPassword()));
			student.setName(dto.getName());
			student.setDepartment(dto.getDepartment());
			student.setMajor(dto.getMajor());
			student.setDateOfBirth(dto.getDateOfBirth());
			student.setGender(dto.getGender());
			student.setCohort(dto.getCohort());
			student.setAcademicAdvior(dto.getAcademicAdvisor());
			student.setDescription(dto.getDescription());
			student.setEnabled(false);
			student.setApprovalStatus(ApprovalStatus.PENDING);
			student.setRegistrationDate(LocalDateTime.now());

			Set<String> authorities = new HashSet<>();
			authorities.add("USER");
			student.setAuthorities(authorities);
			return userRepository.save(student);
		}
	}

	public List<User> getPendingUsers() {
		return userRepository.findByApprovalStatusOrderByRegistrationDateDesc(ApprovalStatus.PENDING);
	}

	public long getPendingUsersCount() {
		return userRepository.countByApprovalStatus(ApprovalStatus.PENDING);
	}

	@Transactional
	public User approveUser(String username) {
		User user = userRepository.findById(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
		user.setEnabled(true);
		user.setApprovalStatus(ApprovalStatus.APPROVED);
		return userRepository.save(user);
	}

	@Transactional
	public User rejectUser(String username) {
		User user = userRepository.findById(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
		user.setEnabled(false);
		user.setApprovalStatus(ApprovalStatus.REJECTED);
		return userRepository.save(user);
	}

	public void createPasswordResetTokenForUser(User user, String token) {
		PasswordResetToken myToken = new PasswordResetToken(token, user);
		passwordResetTokenRepository.save(myToken);
	}

	public Optional<User> getUserByPasswordResetToken(final String token) {
		PasswordResetToken prt = passwordResetTokenRepository.findByToken(token);
		return prt != null ? Optional.ofNullable(prt.getUser()) : Optional.empty();
	}

	public void changeUserPassword(User user, String password) {
		user.setPassword(passwordEncoder.encode(password));
		userRepository.save(user);
	}

	public List<User> getAll() {
		return userRepository.findAll();
	}

	@Transactional
	public User becomeTeacher() {
		User user = getUser();
		if (user != null) {
			Set<String> authorities = user.getAuthorities();
			if (authorities == null) {
				authorities = new HashSet<>();
			}
			authorities.add("TEACHER");
			user.setAuthorities(authorities);
			return userRepository.save(user);
		}
		return null;
	}
}
