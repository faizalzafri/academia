package com.academia.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.academia.platform.model.ClassSection;
import com.academia.platform.model.DayOfWeek;
import com.academia.platform.model.Teacher;
import com.academia.platform.model.TimetableSlot;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long> {
    List<TimetableSlot> findByClassSection(ClassSection classSection);
    List<TimetableSlot> findByTeacher(Teacher teacher);
    List<TimetableSlot> findByTeacherAndDayOfWeekAndPeriodNumber(Teacher teacher, DayOfWeek dayOfWeek, int periodNumber);
    List<TimetableSlot> findByClassSectionAndDayOfWeekAndPeriodNumber(ClassSection classSection, DayOfWeek dayOfWeek, int periodNumber);
}
