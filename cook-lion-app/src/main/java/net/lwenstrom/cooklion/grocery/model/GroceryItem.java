package net.lwenstrom.cooklion.grocery.model;

import net.lwenstrom.cooklion.common.model.AbstractAuditableEntity;
import net.lwenstrom.cooklion.recipe.model.Recipe;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "grocery_items",
       indexes = {
           @Index(name = "idx_grocery_items_list", columnList = "grocery_list_id"),
           @Index(name = "idx_grocery_items_category", columnList = "category"),
           @Index(name = "idx_grocery_items_position", columnList = "position")
       }
)
public class GroceryItem extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grocery_list_id", nullable = false)
    private GroceryList groceryList;

    @NotBlank
    @Size(max = 140)
    @Column(name = "name", nullable = false, length = 140)
    private String name;

    @Size(max = 64)
    @Column(name = "quantity", length = 64)
    private String quantity;

    @Size(max = 64)
    @Column(name = "category", length = 64)
    private String category;

    @Column(name = "checked", nullable = false)
    private boolean checked = false;

    @Column(name = "position", nullable = false)
    private int position = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe sourceRecipe;

    @Column(name = "notes", length = 255)
    private String notes;

    @PrePersist
    private void beforeSave() {
        if (position <= 0) {
            // Auto-assign position if not set
            position = groceryList.getNextItemPosition();
        }
    }

    public void toggleChecked() {
        this.checked = !this.checked;
    }
}
