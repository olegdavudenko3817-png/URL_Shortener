# URL Shortener

Проєкт URL Shortener — це REST API вебсервіс для перетворення довгих URL-адрес у короткі унікальні посилання.

Користувач передає оригінальну URL-адресу, після чого сервіс генерує короткий код і зберігає коротке та оригінальне посилання у PostgreSQL. Перехід за коротким посиланням виконує автоматичне перенаправлення на оригінальну URL-адресу та збільшує лічильник переходів.

## Технологічний стек

### Backend

- Java 25
- Spring Boot
- Spring Data JPA
- Spring Security
- JWT
- BCrypt
- Flyway

### Tests

- JUnit 5
- Mockito
- Spring Boot Test
- Testcontainers

### Database

- PostgreSQL

### Documentation

- OpenAPI 3.0
- Swagger UI

### Build Tool

- Gradle

### Containerization

- Docker
- Docker Compose

### CI

- GitHub Actions

## Локальний запуск

### Передумови

Перед запуском проєкту переконайтеся, що встановлено:

- Java 25 або новішої версії
- Docker Desktop
- Docker Compose

Gradle окремо встановлювати не потрібно, оскільки проєкт використовує Gradle Wrapper.

## Налаштування змінних середовища

У корені проєкту необхідно створити файл `.env`.

Приклад:

```env
DB_HOST=localhost
DB_USERNAME=postgres
DB_PASSWORD=your_database_password
DB_NAME=url_shortener
DB_PORT=5432

APP_PORT=8080
APP_BASE_URL=http://localhost:8080

JWT_SECRET=your_jwt_secret
JWT_EXPIRATION=86400000
```

### Опис змінних

| Змінна | Опис |
|---|---|
| `DB_HOST` | Адреса PostgreSQL |
| `DB_USERNAME` | Ім'я користувача PostgreSQL |
| `DB_PASSWORD` | Пароль PostgreSQL |
| `DB_NAME` | Назва бази даних |
| `DB_PORT` | Порт PostgreSQL |
| `APP_PORT` | Порт застосунку |
| `APP_BASE_URL` | Базова URL-адреса застосунку для формування повних коротких посилань |
| `JWT_SECRET` | Секретний ключ для підпису JWT |
| `JWT_EXPIRATION` | Час дії JWT у мілісекундах |

> Файл `.env` містить конфіденційні дані та не повинен додаватися до Git-репозиторію.

## Запуск через IntelliJ IDEA

Застосунок можна запустити безпосередньо з IntelliJ IDEA через клас:

`src/main/java/com/example/url_shortener/AppLauncher.java`

Перед запуском необхідно переконатися, що PostgreSQL запущено та змінні середовища налаштовані.

Після запуску застосунок буде доступний за адресою:

`http://localhost:8080`

## Запуск через Gradle

Для запуску застосунку:

```bash
./gradlew bootRun
```

Для Windows:

```bash
gradlew.bat bootRun
```

## Збірка проєкту

Для повної збірки:

```bash
./gradlew build
```

Для Windows:

```bash
gradlew.bat build
```

Зібраний JAR-файл буде знаходитися у директорії:

`build/libs/`

Запуск JAR-файлу:

```bash
java -jar build/libs/URL_Shortener-0.0.1-SNAPSHOT.jar
```

## База даних

Для роботи застосунку використовується PostgreSQL.

Структура бази даних керується за допомогою Flyway.

Файли міграцій знаходяться у:

`src/main/resources/db/migration/`

Flyway автоматично перевіряє та застосовує необхідні міграції під час запуску застосунку.

Основні таблиці бази даних:

- `users`
- `short_urls`

## Запуск через Docker Compose

Переконайтеся, що Docker Desktop запущений.

Для запуску PostgreSQL та застосунку:

```bash
docker compose up --build
```

Для запуску у фоновому режимі:

```bash
docker compose up -d --build
```

Після запуску застосунок буде доступний за адресою:

`http://localhost:8080`

### Перегляд логів

```bash
docker compose logs -f
```

### Зупинка сервісів

```bash
docker compose down
```

## Swagger / OpenAPI

Swagger UI:

`http://localhost:8080/swagger-ui.html`

OpenAPI:

`http://localhost:8080/v3/api-docs`

## Версіонування API

API використовує версію `v1`.

Основні маршрути:

- `/api/v1/users`
- `/api/v1/auth`
- `/api/v1/short-urls`

## Аутентифікація

Для аутентифікації користувачів використовується JWT.

### Реєстрація

```http
POST /api/v1/users/register
Content-Type: application/json
```

Приклад:

```json
{
  "username": "john",
  "password": "John1234"
}
```

Пароль повинен:

- містити щонайменше 8 символів;
- містити хоча б одну малу літеру;
- містити хоча б одну велику літеру;
- містити хоча б одну цифру.

### Авторизація

```http
POST /api/v1/auth/login
Content-Type: application/json
```

Приклад:

```json
{
  "username": "john",
  "password": "John1234"
}
```

У відповідь сервіс повертає JWT-токен:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

## Передача JWT Token

Для захищених endpoint необхідно передавати JWT у заголовку:

`Authorization: Bearer <access-token>`

Приклад:

```bash
curl \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
http://localhost:8080/api/v1/users/me
```

## API Endpoints

### Authentication

| Method | Endpoint | Опис |
|---|---|---|
| POST | `/api/v1/auth/login` | Авторизація користувача |

### Users

| Method | Endpoint | Опис |
|---|---|---|
| POST | `/api/v1/users/register` | Реєстрація користувача |
| GET | `/api/v1/users/me` | Отримання даних поточного користувача |

### Short URLs

| Method | Endpoint | Опис |
|---|---|---|
| POST | `/api/v1/short-urls` | Створення короткого посилання |
| GET | `/api/v1/short-urls` | Отримання всіх посилань користувача |
| GET | `/api/v1/short-urls/active` | Отримання активних посилань |
| GET | `/api/v1/short-urls/{id}` | Отримання посилання за ID |
| GET | `/api/v1/short-urls/{id}/statistics` | Отримання статистики посилання |
| PUT | `/api/v1/short-urls/{id}` | Оновлення посилання |
| DELETE | `/api/v1/short-urls/{id}` | Видалення посилання |

### Redirect

```http
GET /{shortCode}
```

Перейти за коротким посиланням може навіть незареєстрований користувач.

Під час переходу сервіс:

1. знаходить коротке посилання;
2. перевіряє строк його дії;
3. збільшує лічильник переходів;
4. перенаправляє користувача на оригінальну URL-адресу.

## Короткі посилання

Для кожного короткого посилання зберігаються:

- ID;
- короткий код;
- оригінальна URL-адреса;
- дата створення;
- кількість переходів;
- дата завершення дії;
- користувач, який створив посилання.

Короткий код генерується за допомогою криптографічно стійкого генератора випадкових чисел (`SecureRandom`) і складається з латинських літер та цифр.

Довжина короткого коду — 8 символів.

## Статистика

Для кожного короткого посилання ведеться статистика переходів.

Отримати статистику можна за допомогою:

```http
GET /api/v1/short-urls/{id}/statistics
```

Приклад відповіді:

```json
{
  "id": 1,
  "shortCode": "e99JDBRC",
  "clickCount": 2
}
```

## Валідація та безпека

У проєкті реалізовано:

- перевірку унікальності імені користувача;
- валідацію пароля;
- BCrypt для хешування паролів;
- JWT-аутентифікацію;
- stateless security;
- у поточній версії ролі користувачів не використовуються, тому JWT-аутентифікація створює `Authentication` без authorities;
- перевірку власника короткого посилання;
- перевірку строку дії посилання;
- валідацію оригінальної URL-адреси;
- зберігання конфіденційних даних у змінних середовища.

## Тестування

### Запуск тестів

```bash
./gradlew clean test
```

### Повна перевірка

```bash
./gradlew check
```

У проєкті використовуються:

- JUnit 5;
- Mockito;
- Spring Boot Test;
- інтеграційні тести;
- Testcontainers;
- PostgreSQL.

Для інтеграційних тестів PostgreSQL запускається за допомогою Testcontainers.

Мінімально необхідне покриття коду тестами — 80%.

## JaCoCo

Для генерації звіту про покриття:

```bash
./gradlew test jacocoTestReport
```

HTML-звіт буде доступний у:

`build/reports/jacoco/test/html/index.html`

## Docker

Проєкт використовує багатостадійний Dockerfile.

На першій стадії виконується збірка застосунку за допомогою Gradle та Java 25.

На другій стадії використовується Java 25 JRE для запуску готового JAR-файлу.

Фінальний контейнер застосунку запускається від непривілейованого користувача.

## CI / GitHub Actions

Для автоматичної перевірки проєкту передбачено використання GitHub Actions.

CI pipeline має виконувати збірку проєкту та запуск тестів.

## Структура проєкту

```text
URL_Shortener
├── .dockerignore
├── .env
├── .gitignore
├── Dockerfile
├── README.md
├── build.gradle
├── compose.yaml
├── gradlew
├── gradlew.bat
├── settings.gradle
│
└── src
    ├── main
    │   ├── java
    │   │   └── com.example.url_shortener
    │   │       ├── auth
    │   │       │   ├── dto
    │   │       ├── AuthController
    │   │       ├── AuthService
    │   │       ├── JwtAuthenticationFilter
    │   │       └── JwtService
    |   |       └── RestAuthenticationEntryPoint
    │   │
    │   │       ├── common
    │   │       │   └── exception
    │   │
    │   │       ├── config
    │   │       │   ├── OpenApiConfig
    │   │       │   ├── SecurityConfig
    │   │       │   └── TimeConfig
    │   │
    │   │       ├── shorturl
    │   │       │   ├── dto
    │   │       ├── RedirectController
    │   │       ├── ShortUrl
    │   │       ├── ShortUrlController
    │   │       ├── ShortUrlMapper
    │   │       ├── ShortUrlRepository
    │   │       ├── ShortUrlSaveService
    │   │       └── ShortUrlService
    │   │
    │   │       ├── user
    │   │       │   ├── dto
    │   │       ├── User
    │   │       ├── UserController
    │   │       ├── UserRepository
    │   │       └── UserService
    │   │
    │   │       └── AppLauncher
    │   │
    │   └── resources
    │       ├── application.properties
    │       └── db
    │           └── migration
    │               ├── V1__create_users.sql
    │               └── V2__create_short_urls.sql
    │
    └── test
        └── java
            └── com.example.url_shortener
                ├── auth
                ├── shorturl
                ├── user
                └── config
```

## Основні можливості

Застосунок підтримує:

- реєстрацію користувачів;
- JWT-аутентифікацію;
- створення коротких URL;
- редагування коротких URL;
- видалення коротких URL;
- перегляд усіх створених посилань;
- перегляд активних посилань;
- перегляд статистики переходів;
- автоматичне перенаправлення;
- підрахунок переходів;
- контроль строку дії посилань;
- PostgreSQL;
- Flyway migrations;
- Swagger/OpenAPI;
- Docker;
- Docker Compose;
- модульне та інтеграційне тестування;
- Testcontainers.
