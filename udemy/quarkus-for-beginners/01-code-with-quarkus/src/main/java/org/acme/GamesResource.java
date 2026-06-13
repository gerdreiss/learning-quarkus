package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.entity.Game;

import java.util.ArrayList;
import java.util.List;

@Path("/games")
public class GamesResource {
    private final List<Game> games;

    public GamesResource() {
        games = new ArrayList<>();
        games.add(new Game(1L, "R6", "FPS"));
        games.add(new Game(2L, "Battlefield 1", "FPS"));
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
            pagedGames = pagedGames.stream()
                    .filter(g -> g.name().toLowerCase().contains(name.toLowerCase()))
                    .toList();
        }

        int total = pagedGames.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);

        if (start >= total) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(pagedGames.subList(start, end)).header("X-Total-Count", total).build();
    }
}
