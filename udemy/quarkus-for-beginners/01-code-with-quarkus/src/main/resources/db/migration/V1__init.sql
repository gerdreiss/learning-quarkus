CREATE SEQUENCE games_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE public.games
(
    id       BIGINT PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL
);
