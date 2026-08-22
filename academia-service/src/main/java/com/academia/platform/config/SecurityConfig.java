package com.academia.platform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.academia.platform.service.UserDetailService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserDetailService userDetailService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> {})
			.headers(headers -> headers
				.contentSecurityPolicy(csp -> csp
					.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.tailwindcss.com https://cdnjs.cloudflare.com; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdnjs.cloudflare.com https://cdn.jsdelivr.net; font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com; img-src 'self' data: https://images.unsplash.com; connect-src 'self';")
				)
				.frameOptions(frame -> frame.deny())
				.httpStrictTransportSecurity(hsts -> hsts
					.includeSubDomains(true)
					.maxAgeInSeconds(31536000)
				)
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
	                "/js/**",
	                "/css/**",
	                "/images/**",
	                "/fonts/**",
	                "/vendor/**",
	                "/register/**",
	                "/login", "/",
	                "/forgotPassword",
	                "/resetPassword",
	                "/user/savePassword",
	                "/user/changePassword",
	                "/updatePassword").permitAll()
				// System Admin Access
				.requestMatchers("/admin/**").hasAuthority("SYSTEM_ADMIN")
				// Principal Governance Access
				.requestMatchers("/principal/**").hasAnyAuthority("PRINCIPAL", "SYSTEM_ADMIN")
				// User & Teacher common routes
				.requestMatchers("/home", "/student/home", "/becomeTeacher", "/user/**", "/chat/**").hasAnyAuthority("USER", "TEACHER", "PRINCIPAL", "SYSTEM_ADMIN")
				// Extra-curricular Activity Management & Viewing
				.requestMatchers("/activities/manage", "/activities/create", "/activities/*/result", "/activities/*/status").hasAnyAuthority("TEACHER", "PRINCIPAL", "SYSTEM_ADMIN")
				.requestMatchers("/activities/**").hasAnyAuthority("USER", "TEACHER", "PRINCIPAL", "SYSTEM_ADMIN")
				// Teacher course management
				.requestMatchers("/manage/course", "/addCourse", "/deleteCourse/**", "/updateCourse/**", "/teacher/course/**").hasAnyAuthority("TEACHER", "PRINCIPAL", "SYSTEM_ADMIN")
				// Academic Admin & Timetable management
				.requestMatchers("/academic/dashboard", "/academic/assign", "/academic/timetable/add", "/academic/timetable/delete/**", "/academic/calendar/add", "/academic/calendar/delete/**").hasAnyAuthority("TEACHER", "PRINCIPAL", "SYSTEM_ADMIN")
				// Course catalog & Academic viewing
				.requestMatchers("/listCourse", "/student/course/**", "/enroll/**", "/academic/timetable/**", "/academic/calendar").hasAnyAuthority("USER", "TEACHER", "PRINCIPAL", "SYSTEM_ADMIN")
				.anyRequest().authenticated()
			)
			.formLogin(form -> form
				.loginPage("/login")
				.permitAll()
				.usernameParameter("username")
				.passwordParameter("password")
				.defaultSuccessUrl("/home", true)
				.failureUrl("/login?error=true")
			)
			.logout(logout -> logout
	            .logoutUrl("/logout")
	            .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
	            .clearAuthentication(true)
	            .invalidateHttpSession(true)
	            .deleteCookies("JSESSIONID")
	            .logoutSuccessUrl("/login?logout=true")
			)
			.authenticationProvider(daoAuthenticationProvider());

		return http.build();
	}
	
	@Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailService);
        auth.setPasswordEncoder(passwordEncoder);
        return auth;
    }

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}
