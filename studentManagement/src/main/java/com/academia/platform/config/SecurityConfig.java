package com.academia.platform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.academia.platform.service.UserDetailService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserDetailService userDetailService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().and()
			.headers()
				.contentSecurityPolicy("default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.tailwindcss.com https://cdnjs.cloudflare.com; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdnjs.cloudflare.com https://cdn.jsdelivr.net; font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com; img-src 'self' data: https://images.unsplash.com; connect-src 'self';")
				.and()
				.frameOptions().deny()
				.httpStrictTransportSecurity()
					.includeSubDomains(true)
					.maxAgeInSeconds(31536000)
				.and()
			.and()
			.authorizeRequests()
				.antMatchers(
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
				.antMatchers("/admin/**").hasAuthority("SYSTEM_ADMIN")
				// Principal Governance Access
				.antMatchers("/principal/**").hasAnyAuthority("PRINCIPAL", "SYSTEM_ADMIN")
				// User & Teacher common routes
				.antMatchers("/home", "/student/home", "/becomeTeacher", "/user/**", "/chat/**").hasAnyAuthority("USER", "TEACHER", "PRINCIPAL", "SYSTEM_ADMIN")
				// Extra-curricular Activity Management & Viewing
				.antMatchers("/activities/manage", "/activities/create", "/activities/*/result", "/activities/*/status").hasAnyAuthority("TEACHER", "PRINCIPAL", "SYSTEM_ADMIN")
				.antMatchers("/activities/**").hasAnyAuthority("USER", "TEACHER", "PRINCIPAL", "SYSTEM_ADMIN")
				// Teacher course management
				.antMatchers("/manage/course", "/addCourse", "/deleteCourse/**", "/updateCourse/**", "/teacher/course/**").hasAnyAuthority("TEACHER", "PRINCIPAL", "SYSTEM_ADMIN")
				// Academic Admin & Timetable management
				.antMatchers("/academic/dashboard", "/academic/assign", "/academic/timetable/add", "/academic/timetable/delete/**", "/academic/calendar/add", "/academic/calendar/delete/**").hasAnyAuthority("TEACHER", "PRINCIPAL", "SYSTEM_ADMIN")
				// Course catalog & Academic viewing
				.antMatchers("/listCourse", "/student/course/**", "/enroll/**", "/academic/timetable/**", "/academic/calendar").hasAnyAuthority("USER", "TEACHER", "PRINCIPAL", "SYSTEM_ADMIN")
				.anyRequest().authenticated()
			.and()
			.formLogin()
				.loginPage("/login")
				.permitAll()
				.usernameParameter("username")
				.passwordParameter("password")
				.defaultSuccessUrl("/home", true)
				.failureUrl("/login?error=true")
			.and()
			.logout()
	            .logoutUrl("/logout")
	            .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
	            .clearAuthentication(true)
	            .invalidateHttpSession(true)
	            .deleteCookies("JSESSIONID")
	            .logoutSuccessUrl("/login?logout=true");
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(daoAuthenticationProvider());
	}
	
	@Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailService);
        auth.setPasswordEncoder(passwordEncoder);
        return auth;
    }
}
