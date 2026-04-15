package com.example.ChronoFlex.service;

import com.example.ChronoFlex.dto.FacultyAccessResponseDTO;
import com.example.ChronoFlex.model.Faculty;
import com.example.ChronoFlex.model.FacultySubjectMap;
import com.example.ChronoFlex.repository.FacultyRepository;
import com.example.ChronoFlex.repository.FacultySubjectMapRepository;
import com.example.ChronoFlex.repository.CollegeClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FacultyAccessService {

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private FacultySubjectMapRepository mapRepository;

    @Autowired
    private CollegeClassRepository classRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ==========================================================
    // 🔹 Authenticate Faculty + Fetch Subjects & Classes
    // ==========================================================
    public FacultyAccessResponseDTO getFacultyAccess(String email, String password) {

        Optional<Faculty> facultyOpt = facultyRepository.findByEmail(email);

        if (facultyOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        Faculty faculty = facultyOpt.get();

        if (!passwordEncoder.matches(password, faculty.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        if (!faculty.getIsActive()) {
            throw new IllegalArgumentException("Faculty account is inactive.");
        }

        // 🔹 Fetch Subjects
        List<FacultyAccessResponseDTO.SubjectInfo> subjects =
                mapRepository.findByFaculty(faculty)
                        .stream()
                        .filter(FacultySubjectMap::getIsActive)
                        .map(map -> new FacultyAccessResponseDTO.SubjectInfo(
                                map.getSubject().getSubjectId(),
                                map.getSubject().getSubjectName(),
                                map.getSemester()
                        ))
                        .toList();

        // 🔹 Fetch Classes (Class Teacher Only)
        List<FacultyAccessResponseDTO.ClassInfo> classes =
                classRepository.findByClassTeacher(faculty)
                        .stream()
                        .map(c -> new FacultyAccessResponseDTO.ClassInfo(
                                c.getClassId(),
                                c.getClassName(),
                                c.getSemester(),
                                c.getSection()
                        ))
                        .toList();

        // 🔹 Build Final Response
        return new FacultyAccessResponseDTO(
                faculty.getFacultyId(),
                faculty.getName(),
                faculty.getDepartment(),
                subjects,
                classes
        );
    }
}