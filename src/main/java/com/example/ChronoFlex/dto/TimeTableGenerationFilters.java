package com.example.ChronoFlex.dto;

import java.util.List;
import java.util.Map;

/**
 * Rule-set to control timetable generation behavior.
 * All fields are optional; if null/empty, defaults are used.
 */
public class TimeTableGenerationFilters {

    /** Max times a subject can appear in a single day. e.g. {"Algorithms": 1, "DBMS": 2} */
    private Map<String, Integer> maxRepeatsPerDay;

    /** Max times a subject can appear in the whole week. e.g. {"Algorithms": 3} */
    private Map<String, Integer> maxRepeatsPerWeek;

    /** Preferred days for lab subjects. e.g. {"DBMS Lab": ["TUE","THU"]} */
    private Map<String, List<String>> preferredDaysForLabs;

    /** If true, avoid scheduling the same subject in consecutive slots within a day. */
    private boolean avoidConsecutiveSameSubject;

    /** If true, respect template’s maxHoursPerFacultyPerDay/Week limits when assigning. */
    private boolean enforceFacultyLimits;

    /** Subjects that should be prioritized for earlier slots each day. */
    private List<String> prioritySubjectsEarly;

    /** How many free (unassigned) slots per day to leave. Default 0. */
    private int freeSlotsPerDay;

    /** How many free (unassigned) slots in the whole week to leave. Default 0. */
    private int freeSlotsPerWeek;

    // Getters & Setters
    public Map<String, Integer> getMaxRepeatsPerDay() { return maxRepeatsPerDay; }
    public void setMaxRepeatsPerDay(Map<String, Integer> maxRepeatsPerDay) { this.maxRepeatsPerDay = maxRepeatsPerDay; }

    public Map<String, Integer> getMaxRepeatsPerWeek() { return maxRepeatsPerWeek; }
    public void setMaxRepeatsPerWeek(Map<String, Integer> maxRepeatsPerWeek) { this.maxRepeatsPerWeek = maxRepeatsPerWeek; }

    public Map<String, List<String>> getPreferredDaysForLabs() { return preferredDaysForLabs; }
    public void setPreferredDaysForLabs(Map<String, List<String>> preferredDaysForLabs) { this.preferredDaysForLabs = preferredDaysForLabs; }

    public boolean isAvoidConsecutiveSameSubject() { return avoidConsecutiveSameSubject; }
    public void setAvoidConsecutiveSameSubject(boolean avoidConsecutiveSameSubject) { this.avoidConsecutiveSameSubject = avoidConsecutiveSameSubject; }

    public boolean isEnforceFacultyLimits() { return enforceFacultyLimits; }
    public void setEnforceFacultyLimits(boolean enforceFacultyLimits) { this.enforceFacultyLimits = enforceFacultyLimits; }

    public List<String> getPrioritySubjectsEarly() { return prioritySubjectsEarly; }
    public void setPrioritySubjectsEarly(List<String> prioritySubjectsEarly) { this.prioritySubjectsEarly = prioritySubjectsEarly; }

    public int getFreeSlotsPerDay() { return freeSlotsPerDay; }
    public void setFreeSlotsPerDay(int freeSlotsPerDay) { this.freeSlotsPerDay = freeSlotsPerDay; }

    public int getFreeSlotsPerWeek() { return freeSlotsPerWeek; }
    public void setFreeSlotsPerWeek(int freeSlotsPerWeek) { this.freeSlotsPerWeek = freeSlotsPerWeek; }
}
