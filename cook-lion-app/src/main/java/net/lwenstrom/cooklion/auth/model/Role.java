package net.lwenstrom.cooklion.auth.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("Standard user with basic privileges"),
    ADMIN("Administrator with full system access");

    private final String description;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}
