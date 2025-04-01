# java-filmorate
Сервис, который работает с фильмами и оценками пользователей, 
а также возвращает топ-10 фильмов, рекомендованных к просмотру

## Схема БД

![Схема #1 к БД java-filmorate](/src/main/resources/diagram_db.png)

## Примеры запросов
<details>
    <summary>ДЛЯ ФИЛЬМОВ:</summary>

* Получение списка всех фильмов:

```SQL
SELECT *
FROM film;
```

* Получение информации по фильму по его id:

```SQL
SELECT *
FROM film
WHERE film.film_id = <?>; -- id фильма
```   

* Получение списка МРА - рейтинга

```SQL
SELECT *
FROM rating_mpa;
```

* Получение списка жанров

```SQL
SELECT *
FROM genre;
```

* Получение списка фильмов с названием жанра

```SQL
SELECT f.name,
        g.name        
FROM film AS f
LEFT JOIN film_genre AS fg ON f.film_id = fg.film_id
LEFT JOIN genre AS g ON fg.genre_id = g.genre_id;
```

* Получение списка фильмов по МРА - рейтингу <?>

```SQL
SELECT f.name, 
        r.name AS rating_name
FROM film f
INNER JOIN rating_mpa r ON f.rating_id = r.rating_id
WHERE r.name = <?>;
```

* Получение топ-? фильмов по количеству лайков:

```SQL
SELECT f.* FROM film f 
                JOIN (  
                    SELECT film_id  
                    FROM film_users
                    GROUP BY film_id
                    ORDER BY COUNT(user_id) DESC
                    LIMIT <?>
                ) AS popular ON f.film_id = popular.film_id
                ORDER BY (SELECT COUNT(*) FROM film_users WHERE film_id = f.film_id) DESC;
```

</details>

<details>
    <summary>ДЛЯ ПОЛЬЗОВАТЕЛЕЙ:</summary>

* Получение списка всех пользователей:

```SQL
SELECT *
FROM users;
```

* Получение информации по пользователю по его id:

```SQL
SELECT *
FROM users
WHERE users.user_id = <?>; -- id пользователя
```

* Получение id и имени друзей по id пользователя = <?>:

```SQL
SELECT u.name,
        u.user_id
FROM users AS u
WHERE u.user_id IN (
    SELECT f.friend_id
    FROM friendship_status AS f
    WHERE f.user_id = <?> 
);
```

* Получение общих друзей двух пользователей user_id = <?> и user_id = <?>

```SQL
SELECT u.*
FROM users u
INNER JOIN friendship_status f1 ON u.user_id = f1.friend_id
INNER JOIN friendship_status f2 ON u.user_id = f2.friend_id
WHERE f1.user_id = <?>
    AND f2.user_id = <?>;
```

</details>
