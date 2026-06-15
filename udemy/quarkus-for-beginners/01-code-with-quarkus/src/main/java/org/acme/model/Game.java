package org.acme.model;

import io.smallrye.common.constraint.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Game model")
public record Game(
    @Schema(description = "Game ID", examples = {"1"})
    @NotNull
    long id,
    @Schema(description = "Game name", examples = {"AtomRPG", "Minecraft"})
    @NotNull
    String name,
    @Schema(description = "Game category", examples = {"FPS", "RPG"})
    @NotNull
    String category
) {
}
