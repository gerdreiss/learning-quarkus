CREATE TABLE public.games
(
    id       bigint PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL
);

create sequence games_id_seq start with 1 increment by 1;
