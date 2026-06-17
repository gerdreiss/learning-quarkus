package org.acme.service;

import io.smallrye.jwt.build.Jwt;
import io.vavr.control.Option;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.User;
import org.acme.model.NewUser;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class UserService {

    public Long createUser(NewUser user) {
        var entity = new User(
            0L,
            user.username(),
            BCrypt.hashpw(user.password(), BCrypt.gensalt()),
            Set.of("admin")
        );
        entity.persist();
        return entity.getId();
    }

    public Option<String> login(String username, String password) {
        Optional<User> entity = User.find("username", username).singleResultOptional();
        return Option
            .ofOptional(entity)
            .filter(user -> BCrypt.checkpw(password, user.getPassword()))
            .map(UserService::signJwt);
    }

    private static String signJwt(User user) {
        return Jwt
            .claims()
            .subject(user.getUsername())
            .groups(user.getRoles())
            .expiresAt(System.currentTimeMillis() / 1000 * 3600)
            .sign();
    }
}
