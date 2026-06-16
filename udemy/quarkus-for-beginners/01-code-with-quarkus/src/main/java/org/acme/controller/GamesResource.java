package org.acme.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.acme.model.Game;
import org.acme.model.NewGame;
import org.acme.service.GameService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.util.Map;

@Path("/games")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Game controller")
public class GamesResource {
    @Inject
    private GameService gameService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get all games",
        description = "Retrieves a paginated list of games. The results can be filtered by game name and sorted by the game category."
    )
    @APIResponse(
        responseCode = "200",
        description = "Retrieves a paginated list of games",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Game.class, type = SchemaType.ARRAY)
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Fails if page number is too high"
    )
    public Response getGames(
        @HeaderParam("page") int page,
        @HeaderParam("size") int size,
        @QueryParam("name") String name,
        @CookieParam("gameCategory") String category
    ) {
        if (name == null || name.isEmpty()) {
            return Response
                .ok(gameService.getGames(page, size).toJavaList())
                .header("X-Total-Count", gameService.countGames(null))
                .build();
        }
        return gameService
            .getGames(name, category, page, size)
            .fold(
                reasonPhrase -> Response
                    .status(Response.Status.NOT_FOUND.getStatusCode(), reasonPhrase)
                    .build(),
                games -> Response
                    .ok(games.toJavaList())
                    .header("X-Total-Count", gameService.countGames(name))
                    .build()
            );
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get game by ID",
        description = "Retrieves a game by its ID."
    )
    @APIResponse(
        responseCode = "404",
        description = "Fails if a game with the given ID does not exist."
    )
    @APIResponse(
        responseCode = "200",
        description = "Retrieves a game by its ID",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Game.class, type = SchemaType.OBJECT)
        )
    )
    public Response getGame(@PathParam("id") long id) {
        return gameService
            .getGame(id)
            .fold(
                () -> Response.status(Response.Status.NOT_FOUND).build(),
                g -> Response
                    .ok(g)
                    .cookie(
                        new NewCookie.Builder("gameCategory")
                            .value(g.category())
                            .path("/")
                            .comment("Games category")
                            .maxAge(3600)
                            .build()
                    )
                    .build()
            );
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Creates a new game",
        description = "Creates a new game."
    )
    @APIResponse(
        responseCode = "201",
        description = "Creates a new game",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Game.class, type = SchemaType.OBJECT)
        )
    )
    public Response createGame(@NonNull NewGame newGame) {
        var newId = gameService.createGame(newGame.name(), newGame.category());
        return Response.created(URI.create("/games/" + newId)).build();
    }

    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Patches a game",
        description = "Patches an existing game."
    )
    public Response updateGame(@PathParam("id") long id, Map<String, String> update) {
        return gameService
            .updateGame(id, update)
            .fold(
                () -> Response.status(Response.Status.NOT_FOUND).build(),
                g -> Response.ok(g).build()
            );
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Replaces a game",
        description = "Replaces a game for the given ID."
    )
    public Response replaceGame(@PathParam("id") long id, @NonNull NewGame gameDTO) {
        return gameService
            .updateGame(id, Map.of("name", gameDTO.name(), "category", gameDTO.category()))
            .fold(
                () -> Response.status(Response.Status.NOT_FOUND).build(),
                g -> Response.ok(g).build()
            );
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Deletes a game",
        description = "Deletes a game for the given ID."
    )
    public Response deleteGame(@PathParam("id") long id) {
        gameService.deleteGame(id);
        return Response.noContent().build();
    }

}
