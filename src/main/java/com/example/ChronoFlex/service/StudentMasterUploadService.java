package com.example.ChronoFlex.service;

import com.example.ChronoFlex.model.Admin;
import com.example.ChronoFlex.model.StudentMasterList;
import com.example.ChronoFlex.repository.StudentMasterListRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StudentMasterUploadService {

    @Autowired
    private StudentMasterListRepository repository;

    public String uploadStudents(MultipartFile file, Admin admin) {

        int success = 0;
        int skipped = 0;
        int failed = 0;

        DataFormatter formatter = new DataFormatter();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return "Excel file is empty.";
            }

            // ✅ Validate Header Row
            Row header = sheet.getRow(0);
            if (header == null ||
                    !formatter.formatCellValue(header.getCell(0)).equalsIgnoreCase("reg_id")) {
                return "Invalid Excel format. First column must be 'reg_id'";
            }

            // ✅ Process Rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String regId = formatter.formatCellValue(row.getCell(0)).trim();
                    String fullName = formatter.formatCellValue(row.getCell(1)).trim();
                    String semester = formatter.formatCellValue(row.getCell(2)).trim();
                    String section = formatter.formatCellValue(row.getCell(3)).trim();
                    String officialEmail = row.getCell(4) != null
                            ? formatter.formatCellValue(row.getCell(4)).trim()
                            : null;

                    // ❗ Skip empty regId rows
                    if (regId.isEmpty()) {
                        failed++;
                        continue;
                    }

                    // ✅ Check Duplicate (collegeCode + regId)
                    Optional<StudentMasterList> existing =
                            repository.findByCollegeCodeAndRegId(
                                    admin.getCollegeCode(), regId);

                    if (existing.isPresent()) {
                        skipped++;
                        continue;
                    }

                    // ✅ Create Student Record
                    StudentMasterList student = new StudentMasterList();
                    student.setCollegeCode(admin.getCollegeCode());
                    student.setRegId(regId);
                    student.setFullName(fullName);
                    student.setSemester(semester);
                    student.setSection(section);
                    student.setOfficialEmail(officialEmail);
                    student.setUploadedByAdminId(admin.getAdminId());
                    student.setCreatedAt(LocalDateTime.now());
                    student.setRegistered(false);
                    student.setStatus("ACTIVE");

                    repository.save(student);
                    success++;

                } catch (Exception e) {
                    failed++;
                }
            }

        } catch (Exception e) {
            return "Error reading Excel file: " + e.getMessage();
        }

        return "Upload Completed → Success: " + success +
                ", Skipped (Duplicates): " + skipped +
                ", Failed: " + failed;
    }


    public Object fetchStudents(String collegeCode,
                                String semester,
                                String section) {

        if (semester != null && section != null) {
            return repository
                    .findByCollegeCodeAndSemesterAndSection(
                            collegeCode, semester, section);
        }

        return repository.findByCollegeCode(collegeCode);
    }

    public String updateStudent(String collegeCode,
                                String regId,
                                StudentMasterList updatedData) {

        StudentMasterList student = repository
                .findByCollegeCodeAndRegId(collegeCode, regId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        student.setFullName(updatedData.getFullName());
        student.setSemester(updatedData.getSemester());
        student.setSection(updatedData.getSection());
        student.setOfficialEmail(updatedData.getOfficialEmail());

        repository.save(student);

        return "Student Updated Successfully";
    }
}