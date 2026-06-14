package org.acme;

import io.vavr.collection.List;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.entity.Game;


@Path("/games")
public class GamesResource {
    private final List<Game> games;

    public GamesResource() {
        games = List.of(
                new Game(1L, "R6", "FPS"),
                new Game(2L, "Battlefield 1", "FPS")
        );
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGames(
            @QueryParam("page") int page,
            @QueryParam("size") int size,
            @QueryParam("name") String name
    ) {

        System.out.printf("GamesResource.getGames: page = %d; size = %d; name = '%s%n'", page, size, name);

        List<Game> pagedGames = games;
        if (name != null && !name.isEmpty()) {
            pagedGames = pagedGames.filter(g -> g.name().toLowerCase().contains(name.toLowerCase()));
        }

        int total = pagedGames.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);

        if (start >= total) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        java.util.List<Game> result = pagedGames.subSequence(start, end).toJavaList();
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
                        g -> Response.ok(g).build()
                );

    }
}
