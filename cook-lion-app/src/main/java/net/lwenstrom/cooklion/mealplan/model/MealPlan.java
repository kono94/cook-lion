package net.lwenstrom.cooklion.mealplan.model;

import net.lwenstrom.cooklion.auth.model.UserAccount;
import net.lwenstrom.cooklion.common.model.AbstractAuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"user", "entries"})
@Entity
@Table(name = "meal_plans",
       indexes = {
           @Index(name = "idx_meal_plans_user", columnList = "user_id"),
           @Index(name = "idx_meal_plans_start_end", columnList = "start_date,end_date"),
           @Index(name = "idx_meal_plans_active", columnList = "is_active")
       }
)
public class MealPlan extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @NotBlank
    @Size(max = 140)
    @Column(name = "title", nullable = false, length = 140)
    private String title;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "mealPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("date ASC, mealType ASC")
    private Set<MealPlanEntry> entries = new LinkedHashSet<>();

    @PrePersist
    @PreUpdate
    private void validateDateRange() {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }

    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return active && 
               (today.isEqual(startDate) || today.isAfter(startDate)) && 
               (today.isEqual(endDate) || today.isBefore(endDate));
    }
}
