package net.lwenstrom.cooklion.grocery.model;

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
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.lwenstrom.cooklion.auth.model.UserAccount;
import net.lwenstrom.cooklion.common.model.AbstractAuditableEntity;
import net.lwenstrom.cooklion.mealplan.model.MealPlan;

@Getter
@Setter
@ToString(exclude = {"user", "mealPlan", "items"})
@Entity
@Table(
        name = "grocery_lists",
        indexes = {
            @Index(name = "idx_grocery_lists_user", columnList = "user_id"),
            @Index(name = "idx_grocery_lists_meal_plan", columnList = "meal_plan_id"),
            @Index(name = "idx_grocery_lists_status", columnList = "status")
        })
public class GroceryList extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id")
    private MealPlan mealPlan;

    @NotBlank
    @Size(max = 140)
    @Column(name = "title", nullable = false, length = 140)
    private String title;

    @Column(name = "status", nullable = false, length = 16)
    private GroceryListStatus status = GroceryListStatus.ACTIVE;

    @OneToMany(mappedBy = "groceryList", orphanRemoval = true)
    @OrderBy("position ASC")
    private Set<GroceryItem> items = new LinkedHashSet<>();

    public int getNextItemPosition() {
        if (items.isEmpty()) {
            return 1;
        } else {
            return items.stream().mapToInt(GroceryItem::getPosition).max().orElse(1) + 1;
        }
    }
}
