MERGE INTO genre (GENRE_ID, NAME) KEY (GENRE_ID) VALUES (1, 'Комедия');
MERGE INTO genre (GENRE_ID, NAME) KEY (GENRE_ID) VALUES (2, 'Драма');
MERGE INTO genre (GENRE_ID, NAME) KEY (GENRE_ID) VALUES (3, 'Мультфильм');
MERGE INTO genre (GENRE_ID, NAME) KEY (GENRE_ID) VALUES (4, 'Триллер');
MERGE INTO genre (GENRE_ID, NAME) KEY (GENRE_ID) VALUES (5, 'Документальный');
MERGE INTO genre (GENRE_ID, NAME) KEY (GENRE_ID) VALUES (6, 'Боевик');

INSERT INTO rating (name, description) VALUES('G', 'У фильма нет возрастных ограничений.');
INSERT INTO rating (name, description) VALUES('PG', 'Детям рекомендуется смотреть фильм с родителями.');
INSERT INTO rating (name, description) VALUES('PG-13', 'Детям до 13 лет просмотр не желателен.');
INSERT INTO rating (name, description) VALUES('R', 'Лицам до 17 лет просматривать фильм можно только в присутствии взрослого.');
INSERT INTO rating (name, description) VALUES('NC-17', 'Лицам до 18 лет просмотр запрещён.');
