package com.academia.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.academia.platform.model.Activity;
import com.academia.platform.model.ActivityParticipant;
import com.academia.platform.model.Student;

@Repository
public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, Long> {
    List<ActivityParticipant> findByActivity(Activity activity);
    List<ActivityParticipant> findByStudent(Student student);
    Optional<ActivityParticipant> findByActivityAndStudent(Activity activity, Student student);
    long countByActivity(Activity activity);
    boolean existsByActivityAndStudent(Activity activity, Student student);
}
