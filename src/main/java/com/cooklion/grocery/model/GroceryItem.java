package com.cooklion.grocery.model;

import com.cooklion.common.model.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "grocery_items",
       indexes = {
           @Index(name = "idx_grocery_items_list", columnList = "grocery_list_id")
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

    @Column(name = "checked", nullable = false)
    private boolean checked = false;

    @Column(name = "position", nullable = false)
    private int position = 0;
}
