CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(256) NOT NULL UNIQUE,
    email         VARCHAR(256) NOT NULL UNIQUE,
    password_hash TEXT         NOT NULL,
    role          VARCHAR(6)   NOT NULL CHECK (role IN ('Player', 'Admin'))
);

CREATE TABLE words
(
    id   BIGSERIAL PRIMARY KEY,
    word VARCHAR(5) NOT NULL UNIQUE
);

CREATE TABLE matches
(
    id         BIGSERIAL PRIMARY KEY,
    word_id    BIGINT      NOT NULL REFERENCES words (id),
    state      VARCHAR(10) NOT NULL CHECK (state IN ('InProgress', 'Finished')),
    start_time TIMESTAMPTZ NOT NULL,
    dead_line  TIMESTAMPTZ NOT NULL
);

CREATE TABLE wordles
(
    id          BIGSERIAL PRIMARY KEY,
    player_id   BIGINT      NOT NULL REFERENCES users (id),
    match_id    BIGINT      NOT NULL REFERENCES matches (id),
    progress    INT         NOT NULL DEFAULT 0,
    state       VARCHAR(10) NOT NULL CHECK (state IN ('InProgress', 'Finished')),
    finish_time TIMESTAMPTZ,
    result      VARCHAR(4) CHECK (result IN ('WIN', 'LOSE', 'TIE'))
);

CREATE TABLE guesses
(
    id      BIGSERIAL PRIMARY KEY,
    g       VARCHAR(5) NOT NULL,
    game_id BIGINT     NOT NULL REFERENCES wordles (id)
);

CREATE TABLE friend_requests
(
    id          BIGSERIAL PRIMARY KEY,
    sender_id   BIGINT     NOT NULL REFERENCES users (id),
    receiver_id BIGINT     NOT NULL REFERENCES users (id),
    state       VARCHAR(8) NOT NULL CHECK (state IN ('Accepted', 'Rejected', 'Pending'))
);

CREATE TABLE match_requests
(
    id          BIGSERIAL PRIMARY KEY,
    sender_id   BIGINT     NOT NULL REFERENCES users (id),
    receiver_id BIGINT     NOT NULL REFERENCES users (id),
    state       VARCHAR(8) NOT NULL CHECK (state IN ('Accepted', 'Rejected', 'Pending')),
    match_id    BIGINT UNIQUE REFERENCES matches (id)
);