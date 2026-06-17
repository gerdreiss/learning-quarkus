package org.acme.model;

import io.smallrye.common.constraint.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Login model")
public record LoginData(
    @Schema(description = "User name")
    @NotNull
    String username,
    @Schema(description = "User password")
    @NotNull
    String password
) {
}
