package com.smartcomplaint.model;

import jakarta.persistence.*;

/**
 * Service Level Agreement (SLA) Configuration rules.
 */
@Entity
@Table(name = "sla_configurations")
public class SlaConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Category category;

    @Column(name = "resolution_hours", nullable = false)
    private Integer resolutionHours;

    @Column(name = "escalation_hours", nullable = false)
    private Integer escalationHours;

    public SlaConfiguration() {
    }

    public SlaConfiguration(Priority priority, Category category, Integer resolutionHours, Integer escalationHours) {
        this.priority = priority;
        this.category = category;
        this.resolutionHours = resolutionHours;
        this.escalationHours = escalationHours;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Integer getResolutionHours() {
        return resolutionHours;
    }

    public void setResolutionHours(Integer resolutionHours) {
        this.resolutionHours = resolutionHours;
    }

    public Integer getEscalationHours() {
        return escalationHours;
    }

    public void setEscalationHours(Integer escalationHours) {
        this.escalationHours = escalationHours;
    }
}

