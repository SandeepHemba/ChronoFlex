package com.example.ChronoFlex.controllers;

import com.example.ChronoFlex.dto.TimetableSlotDTO;
import com.example.ChronoFlex.service.TimeTableViewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/timetable")
public class TimeTableViewController {

    private final TimeTableViewService timeTableViewService;

    public TimeTableViewController(TimeTableViewService timeTableViewService) {
        this.timeTableViewService = timeTableViewService;
    }

    // Endpoint to fetch timetable
    @PostMapping("/view")
    public List<TimetableSlotDTO> getTimeTable(@RequestBody TimetableRequest request) {
        return timeTableViewService.getTimeTable(
                request.getCollegeId(),
                request.getSemester(),
                request.getSection()
        );
    }

    // DTO for request body
    public static class TimetableRequest {
        private Long collegeId;
        private String semester;
        private String section;

        public TimetableRequest() {}

        public TimetableRequest(Long collegeId, String semester, String section) {
            this.collegeId = collegeId;
            this.semester = semester;
            this.section = section;
        }

        public Long getCollegeId() { return collegeId; }
        public void setCollegeId(Long collegeId) { this.collegeId = collegeId; }

        public String getSemester() { return semester; }
        public void setSemester(String semester) { this.semester = semester; }

        public String getSection() { return section; }
        public void setSection(String section) { this.section = section; }
    }
}
