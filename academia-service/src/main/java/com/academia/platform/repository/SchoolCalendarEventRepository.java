package com.academia.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.academia.platform.model.SchoolCalendarEvent;

@Repository
public interface SchoolCalendarEventRepository extends JpaRepository<SchoolCalendarEvent, Long> {
    List<SchoolCalendarEvent> findAllByOrderByEventDateAsc();
}
