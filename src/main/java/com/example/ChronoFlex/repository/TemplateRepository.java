package com.example.ChronoFlex.repository;

import com.example.ChronoFlex.model.College;
import com.example.ChronoFlex.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Integer> {
    List<Template> findByCollege_CollegeCode(String collegeCode);

    List<Template> findByCollege(College college);

    List<Template> findByCollege_CollegeId(Long collegeId);

}
