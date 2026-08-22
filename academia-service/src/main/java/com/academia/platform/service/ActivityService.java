package com.academia.platform.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academia.platform.model.AcademicYear;
import com.academia.platform.model.Activity;
import com.academia.platform.model.ActivityCategory;
import com.academia.platform.model.ActivityParticipant;
import com.academia.platform.model.ActivityResult;
import com.academia.platform.model.ActivityStatus;
import com.academia.platform.model.Student;
import com.academia.platform.model.Teacher;
import com.academia.platform.repository.ActivityParticipantRepository;
import com.academia.platform.repository.ActivityRepository;
import com.academia.platform.repository.ActivityResultRepository;
import com.academia.platform.repository.StudentRepository;
import com.academia.platform.repository.TeacherRepository;

@Service
@Transactional
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityParticipantRepository activityParticipantRepository;

    @Autowired
    private ActivityResultRepository activityResultRepository;

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    public Activity createActivity(String name, String description, ActivityCategory category, Long yearId, LocalDate eventDate, LocalDate regDeadline, String venue, String coordinatorUsername) {
        AcademicYear year = (yearId != null) 
                ? academicYearService.getAcademicYearById(yearId).orElse(academicYearService.getActiveAcademicYear())
                : academicYearService.getActiveAcademicYear();

        Teacher coordinator = null;
        if (coordinatorUsername != null && !coordinatorUsername.isEmpty()) {
            coordinator = teacherRepository.findById(coordinatorUsername).orElse(null);
        }

        Activity activity = new Activity(name, description, category, year, eventDate, venue, coordinator);
        activity.setRegistrationDeadline(regDeadline);
        activity.setStatus(ActivityStatus.REGISTRATION_OPEN);
        return activityRepository.save(activity);
    }

    public List<Activity> getActivitiesForYear(Long yearId) {
        AcademicYear year = (yearId != null) 
                ? academicYearService.getAcademicYearById(yearId).orElse(academicYearService.getActiveAcademicYear())
                : academicYearService.getActiveAcademicYear();
        return activityRepository.findByAcademicYearOrderByEventDateAsc(year);
    }

    public List<Activity> getActivitiesByCategory(Long yearId, ActivityCategory category) {
        AcademicYear year = (yearId != null) 
                ? academicYearService.getAcademicYearById(yearId).orElse(academicYearService.getActiveAcademicYear())
                : academicYearService.getActiveAcademicYear();
        return activityRepository.findByAcademicYearAndCategory(year, category);
    }

    public Optional<Activity> getActivityById(Long id) {
        return activityRepository.findById(id);
    }

    public Activity updateActivityStatus(Long activityId, ActivityStatus status) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activityId));
        activity.setStatus(status);
        return activityRepository.save(activity);
    }

    public String registerStudent(Long activityId, String studentUsername, String role, String teamName) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activityId));

        if (activity.getStatus() == ActivityStatus.COMPLETED || activity.getStatus() == ActivityStatus.CANCELLED) {
            return "Registration is closed for this activity.";
        }

        Student student = studentRepository.findById(studentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentUsername));

        if (activityParticipantRepository.existsByActivityAndStudent(activity, student)) {
            return "Already registered for this activity.";
        }

        ActivityParticipant participant = new ActivityParticipant(activity, student, role != null ? role : "Participant", teamName);
        activityParticipantRepository.save(participant);
        return "success";
    }

    public ActivityResult recordResult(Long activityId, String eventName, String winnerName, String winnerClass, String position, String score) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activityId));

        ActivityResult result = new ActivityResult(activity, eventName, winnerName, winnerClass, position, score);
        return activityResultRepository.save(result);
    }

    public List<ActivityParticipant> getParticipants(Long activityId) {
        Activity activity = activityRepository.findById(activityId).orElse(null);
        return activity != null ? activityParticipantRepository.findByActivity(activity) : List.of();
    }

    public List<ActivityResult> getResults(Long activityId) {
        Activity activity = activityRepository.findById(activityId).orElse(null);
        return activity != null ? activityResultRepository.findByActivity(activity) : List.of();
    }

    public List<ActivityParticipant> getStudentActivities(String studentUsername) {
        Student student = studentRepository.findById(studentUsername).orElse(null);
        return student != null ? activityParticipantRepository.findByStudent(student) : List.of();
    }
}
