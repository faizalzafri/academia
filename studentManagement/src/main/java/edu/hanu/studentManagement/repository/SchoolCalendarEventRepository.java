package edu.hanu.studentManagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hanu.studentManagement.model.SchoolCalendarEvent;

@Repository
public interface SchoolCalendarEventRepository extends JpaRepository<SchoolCalendarEvent, Long> {
    List<SchoolCalendarEvent> findAllByOrderByEventDateAsc();
}
