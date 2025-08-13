package com.cooklion.mealplan.model;

import com.cooklion.common.model.AbstractAuditableEntity;
import com.cooklion.recipe.model.Recipe;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "meal_plan_entries",
       indexes = {
           @Index(name = "idx_meal_plan_entries_plan", columnList = "meal_plan_id"),
           @Index(name = "idx_meal_plan_entries_date", columnList = "date"),
           @Index(name = "idx_meal_plan_entries_recipe", columnList = "recipe_id")
       }
)
public class MealPlanEntry extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 16)
    private MealType mealType = MealType.DINNER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Size(max = 1024)
    @Column(name = "note", length = 1024)
    private String note;
}
