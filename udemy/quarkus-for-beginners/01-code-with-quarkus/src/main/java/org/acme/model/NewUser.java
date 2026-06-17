package org.acme.model;

import io.smallrye.common.constraint.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Set;

@Schema(description = "New user model")
public record NewUser(
    @Schema(description = "User name")
    @NotNull
    String username,
    @Schema(description = "User password")
    @NotNull
    String password,
    @Schema(description = "User roles", type = SchemaType.ARRAY)
    @NotNull
    Set<String> roles
) {
}
