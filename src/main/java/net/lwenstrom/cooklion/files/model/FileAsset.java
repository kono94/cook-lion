package net.lwenstrom.cooklion.files.model;

import net.lwenstrom.cooklion.common.model.AbstractAuditableEntity;
import net.lwenstrom.cooklion.auth.model.UserAccount;
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
@Table(name = "file_assets",
       indexes = {
           @Index(name = "idx_file_assets_owner", columnList = "owner_id"),
           @Index(name = "idx_file_assets_storage_key", columnList = "storage_key", unique = true)
       }
)
public class FileAsset extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    @NotBlank
    @Size(max = 255)
    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    @Size(max = 128)
    @Column(name = "content_type", length = 128)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @NotBlank
    @Size(max = 512)
    @Column(name = "storage_key", nullable = false, unique = true, length = 512)
    private String storageKey;

    @Size(max = 128)
    @Column(name = "checksum_sha256", length = 128)
    private String checksumSha256;

    @Column(name = "publicly_accessible", nullable = false)
    private boolean publiclyAccessible = false;
}
