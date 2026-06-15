package org.acme.model;

import io.smallrye.common.constraint.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "New game model")
public record NewGame(
    @Schema(description = "Game name", examples = {"AtomRPG", "Minecraft"})
    @NotNull
    String name,
    @Schema(description = "Game category", examples = {"FPS", "RPG"})
    @NotNull
    String category
) {
}
