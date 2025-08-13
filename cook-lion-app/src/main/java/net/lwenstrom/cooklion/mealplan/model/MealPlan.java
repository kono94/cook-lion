package net.lwenstrom.cooklion.mealplan.model;

import net.lwenstrom.cooklion.auth.model.UserAccount;
import net.lwenstrom.cooklion.common.model.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
           @Index(name = "idx_meal_plans_start_end", columnList = "start_date,end_date")
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

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "mealPlan", orphanRemoval = true)
    @OrderBy("date ASC, mealType ASC")
    private Set<MealPlanEntry> entries = new LinkedHashSet<>();
}
