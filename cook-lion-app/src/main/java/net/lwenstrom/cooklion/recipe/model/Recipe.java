package net.lwenstrom.cooklion.recipe.model;

import net.lwenstrom.cooklion.auth.model.UserAccount;
import net.lwenstrom.cooklion.common.model.AbstractAuditableEntity;
import net.lwenstrom.cooklion.files.model.FileAsset;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"author", "tags"})
@Entity
@Table(name = "recipes",
       indexes = {
           @Index(name = "idx_recipes_author", columnList = "author_id"),
           @Index(name = "idx_recipes_slug", columnList = "slug", unique = true),
           @Index(name = "idx_recipes_visibility", columnList = "visibility")
       }
)
public class Recipe extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserAccount author;

    @NotBlank
    @Size(max = 140)
    @Column(name = "title", nullable = false, length = 140)
    private String title;

    @NotBlank
    @Size(max = 160)
    @Column(name = "slug", nullable = false, unique = true, length = 160)
    private String slug;

    @Size(max = 4096)
    @Column(name = "summary", length = 4096)
    private String summary;

    @Column(name = "prep_time_minutes")
    private Integer prepTimeMinutes;

    @Column(name = "cook_time_minutes")
    private Integer cookTimeMinutes;

    @Column(name = "servings")
    private Integer servings;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 16)
    private RecipeVisibility visibility = RecipeVisibility.PRIVATE;

    @Size(max = 20000)
    @Column(name = "instructions_markdown", length = 20000)
    private String instructionsMarkdown;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_image_id")
    private FileAsset coverImage;

    @ManyToMany
    @JoinTable(
            name = "recipe_tags",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}
