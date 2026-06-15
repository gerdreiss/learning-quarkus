package org.acme;

import io.vavr.collection.List;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.acme.model.Game;
import org.acme.model.NewGame;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.Map;

import static java.util.function.Function.identity;

@Path("/games")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Game controller")
public class GamesResource {
    private List<Game> games;

    public GamesResource() {
        games = List.of(
            new Game(1L, "Metro 2033", "FPS"),
            new Game(2L, "S.T.A.L.K.E.R.: Clear Sky", "FPS"),
            new Game(3L, "Fallout 4", "FPS"),
            new Game(4L, "AtomRPG", "RPG")
        );
    }

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
        @CookieParam("gameCategory") String gameCategory
    ) {
        List<Game> pagedGames = games;
        if (name != null && !name.isEmpty()) {
            pagedGames = pagedGames.filter(g -> g.name().toLowerCase().contains(name.toLowerCase()));
        }

        int total = pagedGames.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);

        if (start >= total) {
            int statusCode = Response.Status.NOT_FOUND.getStatusCode();
            String reasonPhrase = "Page %d exceed available pages with size %d".formatted(page, size);
            return Response.status(statusCode, reasonPhrase).build();
        }

        var result = pagedGames
            .subSequence(start, end)
            .sortBy(
                (o1, o2) -> {
                    if (o1.category().equalsIgnoreCase(gameCategory)) return -1;
                    if (o2.category().equalsIgnoreCase(gameCategory)) return 1;
                    return 0;
                },
                identity()
            )
            .toJavaList();

        return Response.ok(result).header("X-Total-Count", total).build();
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
        return games
            .find(g -> g.id() == id)
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
    public Response createGame(NewGame newGame) {
        var newId = games.map(Game::id).max().fold(() -> 1L, id -> id + 1);
        games = games.append(new Game(newId, newGame.name(), newGame.category()));
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
        return games.find(g -> g.id() == id)
            .fold(
                () -> Response.status(Response.Status.NOT_FOUND).build(),
                g -> {
                    String newName = update.getOrDefault("name", g.name());
                    String newCategory = update.getOrDefault("category", g.category());
                    var updatedGame = new Game(id, newName, newCategory);
                    games = games.replace(g, updatedGame);
                    return Response.ok(updatedGame).build();
                }
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
    public Response replaceGame(@PathParam("id") long id, NewGame gameDTO) {
        return games.find(g -> g.id() == id)
            .fold(
                () -> Response.status(Response.Status.NOT_FOUND).build(),
                g -> {
                    var newGame = new Game(id, gameDTO.name(), gameDTO.category());
                    games = games.replace(g, newGame);
                    return Response.ok(newGame).build();
                }
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
        games = games.removeFirst(g -> g.id() == id);
        return Response.noContent().build();
    }

}
