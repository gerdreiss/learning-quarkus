package org.acme;

import io.vavr.collection.List;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.acme.entity.Game;

import static java.util.function.Function.identity;

@Path("/games")
public class GamesResource {
    private final List<Game> games;

    public GamesResource() {
        games = List.of(
            new Game(1L, "R6", "FPS"),
            new Game(2L, "Battlefield 1", "FPS"),
            new Game(3L, "Fallout 4", "RPM")
        );
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGames(
        @QueryParam("page") int page,
        @QueryParam("size") int size,
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
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var result = pagedGames.subSequence(start, end).sortBy(
            (o1, o2) -> {
                if (o1.category().equalsIgnoreCase(gameCategory)) return -1;
                if (o2.category().equalsIgnoreCase(gameCategory)) return 1;
                return 0;
            },
            identity()
        ).toJavaList();
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
                g -> Response.ok(g)
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

}
