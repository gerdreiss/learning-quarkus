package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Game;

import java.util.List;

@ApplicationScoped
public class GameRepository implements PanacheRepositoryBase<Game, Long> {
    public List<Game> findPaginated(int page, int size) {
        return findAll()
            .page(Page.of(page, size))
            .list();
    }

    public List<Game> findFilteredAndPaginated(String name, int page, int size) {
        return find("LOWER(name) like LOWER(?1)", "%" + name + "%").list();
    }

    public long countByName(String name) {
        return count("name like ?1", "%" + name + "%");
    }
}
