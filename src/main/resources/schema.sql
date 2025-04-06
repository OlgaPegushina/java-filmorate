DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS friendship_status CASCADE;
DROP TABLE IF EXISTS rating_mpa CASCADE;
DROP TABLE IF EXISTS film CASCADE;
DROP TABLE IF EXISTS film_users CASCADE;
DROP TABLE IF EXISTS genre CASCADE;
DROP TABLE IF EXISTS film_genre CASCADE;
DROP TABLE IF EXISTS director CASCADE;
DROP TABLE IF EXISTS director_film CASCADE;
DROP TABLE IF EXISTS review CASCADE;
DROP TABLE IF EXISTS review_users CASCADE;

CREATE TABLE IF NOT EXISTS rating_mpa (
    rating_id INTEGER PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS film (
    film_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR NOT NULL,
    description VARCHAR(200),
    release_date DATE NOT NULL,
    duration_in_minutes BIGINT NOT NULL,
    rating_id INTEGER,
    FOREIGN KEY (rating_id) REFERENCES rating_mpa(rating_id)
);

CREATE TABLE IF NOT EXISTS genre (
    genre_id INTEGER PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS film_genre (
    film_id BIGINT,
    genre_id INTEGER,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genre(genre_id)
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR NOT NULL UNIQUE,
    login VARCHAR NOT NULL UNIQUE,
    name VARCHAR NOT NULL,
    birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS film_users (
    film_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS friendship_status (
    user_id BIGINT,
    friend_id BIGINT,
    is_status BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS director (
    director_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS director_film (
    director_id BIGINT,
    film_id BIGINT,
    PRIMARY KEY (director_id, film_id),
    FOREIGN KEY (director_id) REFERENCES director(director_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (film_id) REFERENCES film(film_id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review (
    review_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(255),
    is_positive BOOLEAN NOT NULL,
    FOREIGN KEY (film_id) REFERENCES film(film_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review_users (
    review_id BIGINT,
    user_id BIGINT,
    rating INTEGER DEFAULT 0,
    PRIMARY KEY (review_id, user_id),
    FOREIGN KEY (review_id) REFERENCES review(review_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

