package org.acme.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.acme.model.LoginData;
import org.acme.model.NewUser;
import org.acme.service.UserService;
import org.jspecify.annotations.NonNull;

import java.net.URI;

import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @POST
    public Response register(NewUser user) {
        var id = userService.createUser(user);
        return Response.created(URI.create("/users/" + id)).build();
    }

    @POST
    @Path("/login")
    public Response login(@NonNull LoginData loginData) {
        return userService
            .login(loginData.username(), loginData.password())
            .fold(
                () -> Response
                    .status(UNAUTHORIZED.getStatusCode(), "Invalid username or password")
                    .build(),
                jwt -> Response.ok()
                    .cookie(
                        new NewCookie.Builder("JWT")
                            .value(jwt)
                            .path("/")
                            .comment("JWT")
                            .maxAge(3600)
                            .secure(true)
                            .httpOnly(true)
                            .build()
                    )
                    .build()
            );
    }
}
