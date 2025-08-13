package net.lwenstrom.cooklion.auth.model;

import net.lwenstrom.cooklion.common.model.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString(exclude = {"user"})
@Entity
@Table(name = "refresh_tokens",
       indexes = {
           @Index(name = "idx_refresh_tokens_token_hash", columnList = "token_hash", unique = true),
           @Index(name = "idx_refresh_tokens_user", columnList = "user_id"),
           @Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at")
       }
)
public class RefreshToken extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @NotBlank
    @Size(min = 64, max = 128)
    @Column(name = "token_hash", nullable = false, unique = true, length = 128)
    private String tokenHash;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Size(max = 128)
    @Column(name = "replaced_by_token_hash", length = 128)
    private String replacedByTokenHash;

    @Size(max = 64)
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Size(max = 512)
    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "is_valid", nullable = false)
    private boolean valid = true;

    public boolean isValid() {
        return valid && 
               expiresAt.isAfter(Instant.now()) && 
               revokedAt == null;
    }

    public void invalidate() {
        this.valid = false;
        this.revokedAt = Instant.now();
    }
}
