package edu.hanu.studentManagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hanu.studentManagement.model.ClassSection;
import edu.hanu.studentManagement.model.DayOfWeek;
import edu.hanu.studentManagement.model.Teacher;
import edu.hanu.studentManagement.model.TimetableSlot;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long> {
    List<TimetableSlot> findByClassSection(ClassSection classSection);
    List<TimetableSlot> findByTeacher(Teacher teacher);
    List<TimetableSlot> findByTeacherAndDayOfWeekAndPeriodNumber(Teacher teacher, DayOfWeek dayOfWeek, int periodNumber);
    List<TimetableSlot> findByClassSectionAndDayOfWeekAndPeriodNumber(ClassSection classSection, DayOfWeek dayOfWeek, int periodNumber);
}
