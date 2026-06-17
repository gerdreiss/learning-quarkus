package org.acme.service;

import io.vavr.collection.List;
import io.vavr.control.Option;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.Game;
import org.acme.repository.GameRepository;
import org.jspecify.annotations.NonNull;

import java.util.Map;

@Dependent
public class GameService {
    @Inject
    private GameRepository gameRepository;

    public long countGames(String name) {
        if (name == null || name.isEmpty()) {
            return gameRepository.count();
        }
        return gameRepository.countByName(name);
    }

    public List<Game> getGames(int page, int size) {
        return List
            .ofAll(gameRepository.findPaginated(page, size))
            .map(e -> new Game(e.getId(), e.getName(), e.getCategory()));
    }

    public List<Game> getGames(String name, String gameCategory, int page, int size) {
        return List
            .ofAll(gameRepository.findFilteredAndPaginated(name, page, size))
            .map(e -> new Game(e.getId(), e.getName(), e.getCategory()))
            .sorted((o1, o2) -> {
                if (o1.category().equalsIgnoreCase(gameCategory)) return -1;
                if (o2.category().equalsIgnoreCase(gameCategory)) return 1;
                return 0;
            });
    }

    public Option<Game> getGame(Long id) {
        var result = gameRepository
            .findByIdOptional(id)
            .map(e -> new Game(e.getId(), e.getName(), e.getCategory()));

        return Option.ofOptional(result);
    }

    @Transactional
    public Long createGame(String name, String category) {
        return gameRepository.persist(name, category);
    }

    @Transactional
    public Option<Game> updateGame(Long id, @NonNull Map<String, String> update) {
        return Option
            .ofOptional(gameRepository.findByIdOptional(id))
            .map(e -> {
                e.setName(update.getOrDefault("name", e.getName()));
                e.setCategory(update.getOrDefault("category", e.getCategory()));
                return e;
            })
            .peek(e -> gameRepository.persist(e))
            .map(e -> new Game(e.getId(), e.getName(), e.getCategory()));
    }

    @Transactional
    public void deleteGame(Long id) {
        gameRepository.deleteById(id);
    }
}
