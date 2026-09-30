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

Покрыты: сервисы (Mockito), контроллеры (MockMvc), репозиторий на реальном PostgreSQL
(Testcontainers) — включая проверку отсутствия N+1.

## Настройка

Параметры подключения к БД — в [`src/main/resources/application.yml`](src/main/resources/application.yml):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/rbac
    username: rbac
    password: rbac
```

Параметры PostgreSQL задаются в [`docker-compose.yml`](docker-compose.yml)
(`POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD`, порт `5432`).

## Структура проекта

```
src/main/java/com/example/rbac/
├── RbacApplication.java        # точка входа
├── config/OpenApiConfig.java   # метаданные OpenAPI
├── entity/                     # User, Role, Permission (JPA)
├── dto/                        # CreateUserRequest, UserDto, seed-данные, ошибки
├── exception/                  # BadRequestException, NotFoundException
├── repository/                 # репозитории + кастомная выборка (fetch-join)
├── service/                    # UserService, RoleService, ImportService
├── spec/UserSpecifications.java# динамические фильтры
└── web/                        # контроллеры + обработка ошибок
src/main/resources/
├── application.yml
├── db/import-data.json         # тестовые данные (JSON, загрузка через POST /api/import)
└── db/migration/               # Flyway-миграции
    ├── V1__init_schema.sql     # DDL: таблицы, FK, индексы
    └── V2__seed_data.sql       # тестовые данные
```

## Ключевые решения

**Схема БД.** many-to-many материализованы join-таблицами `user_roles` и `role_permissions` с составным PK: он
гарантирует уникальность пары (идемпотентная выдача) и индексирует оба направления обхода. `ON DELETE CASCADE` не
оставляет осиротевших связей. Индексы: `UNIQUE` по `users.email`, `roles.name`, `permissions.code` — под точные
совпадения; обратные колонки join-таблиц (`role_id`, `permission_id`) — под фильтры «какие пользователи имеют
роль/разрешение X»; `users(active)` — под частый фильтр.

**JPA.** `@ManyToMany(fetch = LAZY)` + `@JoinTable`: `EAGER` подтягивал бы роли и разрешения при каждом `SELECT users`,
даже
когда не нужны. Маппинг в DTO — внутри `@Transactional` сервиса (иначе `LazyInitializationException`).

**N+1 и пагинация.** `JpaSpecificationExecutor` не поддерживает fetch-join, а fetch-join несовместим с
`setFirstResult/setMaxResults` (Hibernate применяет лимит в памяти, HHH90003004), поэтому выборка собрана вручную
в два этапа (Criteria API, `UserRepositoryImpl`): (1) страница `id` с фильтрами, сортировкой и `LIMIT/OFFSET` —
на уровне БД; (2) граф `User → roles → permissions` одним `LEFT JOIN` только по этим `id`; плюс отдельный `count` —
всего 3 ограниченных запроса на страницу вместо 1+2N. Проверено тестом через `SessionFactory` statistics.

**Фильтрация.** `Specification` с динамическими предикатами: 4 опциональных фильтра без 15 комбинаций
`:param IS NULL OR ...` из JPQL. Фильтры по роли/разрешению — EXISTS-сабзапросы (Criteria API не имеет `Path.exists()`).
Пагинация и count — стандартными средствами `Pageable`.

**Валидация.** На DTO, а не на сущностях: сущность описывает БД, DTO — правила входа (например, `roleIds` не является
полем таблицы). Ошибки — `400` с телом `{ "errors": [ { "field", "message" } ] }` через `@RestControllerAdvice`.
Существование `roleIds` проверяет сервис — это целостность данных, а не Bean Validation.

**Ответ страницы.** Контроллер возвращает `PageResponse<T>` (`content`, `page`, `size`, `totalElements`,
`totalPages`), а не `Page` напрямую: сериализация `PageImpl` тянет дубли (`pageable`, `number`/`page`,
`size`, `sort` дважды, `first/last/empty`) и не совпадает с OpenAPI-контрактом.

**Миграции.** Схема и seed-данные — Flyway-миграции в `src/main/resources/db/migration/`
(`V1__init_schema.sql`, `V2__seed_data.sql`), применяются при старте приложения. `ddl-auto: none` —
Hibernate схему не трогает. В тестах (`@DataJpaTest`) Flyway отключён: схема создаётся Hibernate
`create-drop`, данные загружает сам тест. Тестовые данные дублируются в JSON
(`db/import-data.json`) и перезагружаются через `POST /api/import` — идемпотентно: существующие
сущности обновляются по уникальному ключу, отсутствующие создаются.

**OpenAPI.** Спецификация генерируется из кода: `@Operation`/`@Parameter`/`@ApiResponse` на контроллерах,
`@Schema(description, example)` на DTO. `page`/`size`/`sort` — явные `@RequestParam` (springdoc не разворачивает
`Pageable` в отдельные параметры), `produces = application/json` — иначе медиа-тип деградирует до `*/*`.

## Дополнительные материалы

- [`tasks/`](tasks/) — задания на разработку этого приложения (по OpenAPI / по SQL / по легенде).

## Остановка

```bash
# остановить приложение — Ctrl+C (или остановить процесс java)
# остановить и удалить PostgreSQL
docker compose down
# остановить и удалить PostgreSQL вместе с данными
docker compose down -v
```
