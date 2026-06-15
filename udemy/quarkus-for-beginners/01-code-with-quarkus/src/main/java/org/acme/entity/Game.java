package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Game extends PanacheEntityBase {
    @Id
    @GeneratedValue(generator = "games_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "games_id_seq", sequenceName = "games_id_seq", allocationSize = 1)
    private long id;
    private String name;
    private String category;

    public Game(long id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public Game() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Game that = (Game) o;
        return this.id == that.id &&
            Objects.equals(this.name, that.name) &&
            Objects.equals(this.category, that.category);
    }
}
