CREATE SEQUENCE user_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE public.users
(
    id       BIGINT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT UK_user_username UNIQUE (username)
);

CREATE TABLE public.user_roles
(
    user_id BIGINT       NOT NULL,
    roles   VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, roles),
    CONSTRAINT FK_user_roles_users FOREIGN KEY (user_id) REFERENCES public.users (id)
);