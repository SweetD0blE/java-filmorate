--таблица жанров
DROP TABLE IF EXISTS genre CASCADE;
CREATE TABLE genre (
	genre_id integer AUTO_INCREMENT PRIMARY KEY ,
	name CHARACTER(40)
);

--таблица МРА-рейтингов
DROP TABLE IF EXISTS rating CASCADE;
CREATE TABLE rating (
	rating_id int AUTO_INCREMENT PRIMARY KEY ,
	name CHARACTER(40) ,
	description CHARACTER(40)
);

--таблица film_genre
DROP TABLE IF EXISTS film_genre CASCADE;
CREATE TABLE film_genre (
    film_id int,
    genre_id int,
    constraint pk_film_genre primary key (film_id, genre_id)
);

--таблица фильмов
DROP TABLE IF EXISTS films CASCADE;
CREATE TABLE films (
    film_id int AUTO_INCREMENT PRIMARY KEY ,
    name character(100),
    description character(200),
    release_date date,
    duration int,
    rating_id int
);

--таблица лайков фильмов
DROP TABLE IF EXISTS likes CASCADE;
CREATE TABLE likes (
    user_id int,
    film_id int,
    constraint pk_likes primary key (film_id, user_id)
);

--таблица пользователей
DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE users (
    user_id int AUTO_INCREMENT PRIMARY KEY ,
    email character(100),
    login character(100),
    name character(100),
    birthday date
);

--таблица дружбы
DROP TABLE IF EXISTS friendships CASCADE;
CREATE TABLE friendships (
    user_id int,
    friend_id int,
    status boolean DEFAULT FALSE,
    constraint pk_friendships primary key (user_id, friend_id)
);

--связывание внешних ключей
ALTER TABLE likes ADD CONSTRAINT fk_likes_film_id FOREIGN KEY(film_id)
REFERENCES films (film_id);

ALTER TABLE likes ADD CONSTRAINT fk_likes_user_id FOREIGN KEY(user_id)
REFERENCES users (user_id);

ALTER TABLE friendships ADD CONSTRAINT pk_friendships_user_id FOREIGN KEY(user_id)
REFERENCES users (user_id);

ALTER TABLE friendships ADD CONSTRAINT pk_friendships_friend_id FOREIGN KEY(friend_id)
REFERENCES users (user_id);

ALTER TABLE films ADD CONSTRAINT fk_rating_id FOREIGN KEY(rating_id)
REFERENCES mpa_rating (id);

ALTER TABLE film_genre ADD CONSTRAINT fk_film_genre_film_id FOREIGN KEY(film_id)
REFERENCES films (film_id);

ALTER TABLE film_genre ADD CONSTRAINT fk_film_genre_genre_id FOREIGN KEY(genre_id)
REFERENCES genres (genre_id);