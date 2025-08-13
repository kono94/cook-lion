package net.lwenstrom.cooklion.recipe.model;

import net.lwenstrom.cooklion.common.model.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tags",
       indexes = {
           @Index(name = "idx_tags_name", columnList = "name", unique = true),
           @Index(name = "idx_tags_slug", columnList = "slug", unique = true)
       }
)
public class Tag extends AbstractAuditableEntity {

    @NotBlank
    @Size(max = 64)
    @Column(name = "name", nullable = false, unique = true, length = 64)
    private String name;

    @NotBlank
    @Size(max = 64)
    @Column(name = "slug", nullable = false, unique = true, length = 64)
    private String slug;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    private String description;
}
