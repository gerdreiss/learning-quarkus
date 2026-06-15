package org.acme;

import io.vavr.collection.List;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.acme.model.Game;
import org.acme.model.NewGame;

import java.net.URI;
import java.util.Map;

import static java.util.function.Function.identity;

@Path("/games")
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
    public Response createGame(NewGame newGame) {
        var newId = games.map(Game::id).max().fold(() -> 1L, id -> id + 1);
        games = games.append(new Game(newId, newGame.name(), newGame.category()));
        return Response.created(URI.create("/games/" + newId)).build();
    }

    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
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
    public Response deleteGame(@PathParam("id") long id) {
        games = games.removeFirst(g -> g.id() == id);
        return Response.noContent().build();
    }

}
