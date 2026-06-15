package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Game;

@ApplicationScoped
public class GameRepository implements PanacheRepositoryBase<Game, Long> {
}
