package org.acme.service;

import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.acme.model.Game;
import org.acme.repository.GameRepository;

import java.util.Map;

import static java.util.function.Function.identity;

@Dependent
public class GameService {
    @Inject
    private GameRepository gameRepository;

    private List<Game> games;

    public GameService() {
        games = List.of(
            new Game(1L, "Metro 2033", "FPS"),
            new Game(2L, "S.T.A.L.K.E.R.: Clear Sky", "FPS"),
            new Game(3L, "Fallout 4", "FPS"),
            new Game(4L, "AtomRPG", "RPG")
        );
    }

    public Either<String, List<Game>> getGames(String name, String gameCategory, int page, int size) {
        List<Game> pagedGames = List
            .ofAll(gameRepository.listAll())
            .map(entity -> new Game(entity.getId(), entity.getName(), entity.getCategory()));

        if (name != null && !name.isEmpty()) {
            pagedGames = pagedGames.filter(g -> g.name().toLowerCase().contains(name.toLowerCase()));
        }

        int total = pagedGames.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);

        if (start >= total) {
            return Either.left("Page %d exceed available pages with size %d".formatted(page, size));
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
            );

        return Either.right(result);
    }

    public Option<Game> getGame(Long id) {
        return games.find(g -> id == g.id());
    }

    public Long createGame(String name, String category) {
        var newId = games.map(Game::id).max().fold(() -> 1L, id -> id + 1);
        games = games.append(new Game(newId, name, category));
        return newId;
    }

    public Option<Game> updateGame(Long id, Map<String, String> update) {
        return games
            .find(g -> g.id() == id)
            .map(g -> {
                String newName = update.getOrDefault("name", g.name());
                String newCategory = update.getOrDefault("category", g.category());
                var updatedGame = new Game(id, newName, newCategory);
                games = games.replace(g, updatedGame);
                return updatedGame;
            });
    }

    public void deleteGame(Long id) {
        games = games.removeFirst(g -> g.id() == id);
    }
}
