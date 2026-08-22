package com.academia.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.academia.platform.model.Activity;
import com.academia.platform.model.ActivityResult;

@Repository
public interface ActivityResultRepository extends JpaRepository<ActivityResult, Long> {
    List<ActivityResult> findByActivity(Activity activity);
}
