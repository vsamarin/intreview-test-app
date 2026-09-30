# RBAC API

Мини-сервис управления пользователями, ролями и разрешениями (RBAC) на Spring Boot.
Связи many-to-many: `User ↔ Role ↔ Permission`; разрешения пользователя складываются из его ролей.

## Возможности

- `GET /api/users` — поиск с фильтрацией (email, роль, разрешение, активность), пагинацией и сортировкой.
- `POST /api/users` — создание пользователя с валидацией.
- `POST /api/roles/{roleId}/permissions/{permissionId}` — идемпотентная выдача разрешения роли.
- `POST /api/import` — идемпотентная загрузка тестовых данных из `db/import-data.json`.
- OpenAPI-спецификация, генерируемая из кода (springdoc-openapi).

## Стек

| Компонент                   | Версия |
|-----------------------------|--------|
| Java                        | 21     |
| Spring Boot                 | 3.5.x  |
| Spring Data JPA / Hibernate | 6.x    |
| PostgreSQL                  | 16     |
| Flyway                      | 11.x   |
| springdoc-openapi           | 2.8.x  |
| Сборка                      | Maven  |

## Требования

- Java 21 (JDK)
- Maven 3.8+
- Docker + Docker Compose (для PostgreSQL)

## Быстрый старт

```bash
# 1. Поднять PostgreSQL (пустая БД, данные хранятся в named volume pgdata)
docker compose up -d

# 2. Собрать и запустить приложение
mvn spring-boot:run
```

Приложение будет доступно на `http://localhost:8080`.

> Схема БД и тестовые данные (4 пользователя, 3 роли, 5 разрешений) создаются
> **Flyway** при старте приложения миграциями из
> [`src/main/resources/db/migration/`](src/main/resources/db/migration/)
> (`V1__init_schema.sql`, `V2__seed_data.sql`). История применений — в таблице
> `flyway_schema_history`. Чистая БД: `docker compose down -v && docker compose up -d`.

### Альтернативный запуск (jar)

```bash
mvn -DskipTests package          # соберет target/testProj1-1.0-SNAPSHOT.jar
java -jar target/testProj1-1.0-SNAPSHOT.jar
```

## Документация API

| Ресурс             | Ссылка                                |
|--------------------|---------------------------------------|
| **Swagger UI**     | http://localhost:8080/swagger-ui.html |
| **OpenAPI (JSON)** | http://localhost:8080/v3/api-docs     |

### Примеры запросов

```bash
# Фильтрация и пагинация
curl "http://localhost:8080/api/users?email=admin"
curl "http://localhost:8080/api/users?role=ADMIN&active=true"
curl "http://localhost:8080/api/users?permission=USER_READ&page=0&size=10&sort=email,asc"
curl "http://localhost:8080/api/users?role=USER&active=false"

# Создание пользователя (roleIds — существующие id ролей)
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"email":"newuser@example.com","name":"Новый пользователь","roleIds":[3]}'

# Выдать разрешение роли (идемпотентно)
curl -X POST http://localhost:8080/api/roles/3/permissions/2

# Перезагрузить тестовые данные (идемпотентно, источник — db/import-data.json)
curl -X POST http://localhost:8080/api/import
```

## Тесты

```bash
mvn test
```

## Остановка

```bash
# остановить приложение — Ctrl+C (или остановить процесс java)
# остановить и удалить PostgreSQL
docker compose down
# остановить и удалить PostgreSQL вместе с данными
docker compose down -v
```
