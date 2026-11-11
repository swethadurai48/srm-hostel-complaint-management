package com.smartcomplaint.repository;

import com.smartcomplaint.model.Category;
import com.smartcomplaint.model.Priority;
import com.smartcomplaint.model.SlaConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaConfigurationRepository extends JpaRepository<SlaConfiguration, Long> {
    Optional<SlaConfiguration> findByPriorityAndCategory(Priority priority, Category category);
    Optional<SlaConfiguration> findByPriorityAndCategoryIsNull(Priority priority);
}

