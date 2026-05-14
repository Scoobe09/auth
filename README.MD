# Auth Service — сервис аутентификации, backend-сторона от команды МГТУ им. Н.Э. Баумана

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen)
![Spring Security](https://img.shields.io/badge/Spring%20Security-7.0.5-blue)
![Java](https://img.shields.io/badge/Java-25-red)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED)

REST-сервис для аутентификации и управления пользователями. Реализована регистрация, вход, верификация email по одноразовому коду (OTP), сброс пароля, защищённые профили. Безопасность на основе **JWT** с передачей через http-only cookie, гибкая настройка CORS, готовая документация OpenAPI и контейнеризация.

## Возможности

- **Регистрация** нового пользователя с хешированием пароля (BCrypt) и приветственным письмом
- **Аутентификация** по email и паролю, возврат JWT в теле ответа и в http?only cookie
- **Верификация email** через 6-значный OTP, действительный 10 минут
- **Сброс пароля** с отправкой кода на почту
- **Защищённый профиль** — доступ только по токену
- **Красивые HTML-письма** в корпоративном стиле МГТУ (Thymeleaf)
- **Документация OpenAPI 3.0** (swagger?spec) вручную, без лишних библиотек
- **Полная контейнеризация** (Docker Compose) с PostgreSQL

## Стек технологий

| Область             | Технологии                                   |
|---------------------|----------------------------------------------|
| Язык                | Java 25                                      |
| Фреймворк           | Spring Boot 4.0.6, Spring Security 7.0.5     |
| Работа с данными     | Spring Data JPA, Hibernate, PostgreSQL       |
| Безопасность         | JWT (jjwt 0.12.6), BCrypt, http-only cookies |
| Почта               | Spring Mail + Thymeleaf (HTML-письма)        |
| Документация        | OpenAPI 3.0 (YAML), Postman коллекция        |
| Контейнеризация     | Docker, Docker Compose                       |

## Руководство пользователя (локальная разработка)

### Предварительные требования

- Java 25
- Maven 3.9+
- PostgreSQL (или Docker для базы)
- SMTP-доступ (Яндекс / другой сервис) или отключение отправки писем для теста

### 1. Клонирование репозитория

```bash
git clone https://github.com/PeredereevIN/auth-service.git
cd auth-service
```

### 2. Настройка переменных окружения

Создайте `.env` файл в корне. Пример:

```ini
DB_PASSWORD=postgres
JWT_SECRET=ваш_base64_ключ_минимум_32_байта
MAIL_HOST=smtp.yandex.ru
MAIL_PORT=587
MAIL_USERNAME=your@yandex.ru
MAIL_PASSWORD=пароль_приложения
```

> **Важно:** `JWT_SECRET` должен быть Base64 строкой длиной не менее 32 байт (44 символа). Сгенерировать: `openssl rand -base64 32`

### 3. Локальный запуск

```bash
# Убедитесь, что PostgreSQL запущен и создайте базу auth_service
mvn spring-boot:run
```

Приложение поднимется на `http://localhost:8080/api/v1`.

## Запуск через Docker Compose

```bash
docker-compose up --build -d
```

Автоматически поднимутся два контейнера:
- **auth-db** – PostgreSQL (порт 5432)
- **auth-app** – само приложение (порт 8080)

Все переменные окружения пробрасываются из `.env`.

## API Документация

- **OpenAPI спецификация**: `src/main/resources/static/openapi.yaml`  
  Доступна по адресу: `http://localhost:8080/api/v1/openapi.yaml`
- **Swagger UI**: откройте [Swagger Editor](https://editor.swagger.io) и вставьте содержимое `openapi.yaml` или укажите URL.
- **Postman коллекция**: [скачать коллекцию](#) (скоро появится ссылка)

### Основные эндпоинты

| Метод   | URL                     | Доступ         | Описание                              |
|---------|-------------------------|----------------|---------------------------------------|
| `POST`  | `/api/v1/login`         | Публичный      | Вход, получение JWT                   |
| `GET`   | `/api/v1/is-authenticated` | Защищённый   | Проверка аутентификации               |
| `POST`  | `/api/v1/register`      | Публичный      | Регистрация                           |
| `GET`   | `/api/v1/profile`       | Защищённый     | Профиль текущего пользователя         |
| `POST`  | `/api/v1/send-otp`      | Защищённый     | Отправка OTP для верификации email    |
| `POST`  | `/api/v1/verify-otp`    | Защищённый     | Подтверждение email по коду           |
| `POST`  | `/api/v1/send-reset-otp`| Публичный      | Отправка кода для сброса пароля       |
| `POST`  | `/api/v1/reset`         | Публичный      | Установка нового пароля по коду       |

> Защищённые эндпоинты требуют JWT в заголовке `Authorization: Bearer <токен>` либо автоматически через cookie `jwt`.

## Архитектура и структура проекта

```
src/
    main/
        java/com/peredereevin/authservice/
            config/          # Конфигурация Spring Security, CORS, OpenAPI
            controller/      # Контроллеры (Auth, Profile, Reset)
            entity/          # JPA-сущности (User)
            filter/          # JWT-фильтр
            io/              # DTO (запросы/ответы)
            repository/      # Spring Data репозитории
            service/         # Бизнес-логика (аутентификация, профиль, OTP, email)
            util/            # Утилиты (JWT)
        resources/
            templates/mail/  # HTML-шаблоны писем (Thymeleaf)
            static/
                openapi.yaml # OpenAPI спецификация
            application.yaml # Конфигурация приложения
```

## Безопасность

- **Пароли** хранятся в виде BCrypt-хешей.
- **JWT** генерируется после успешного входа, срок жизни — 10 часов.
- JWT передаётся в **http-only cookie** и также в теле ответа (для гибкости фронтенда).
- Фильтр `JwtRequestFilter` извлекает токен из заголовка `Authorization` или cookie, загружает пользователя и кладёт аутентификацию в `SecurityContext`.
- Публичные эндпоинты (`/login`, `/register`, `/send?reset?otp`, `/reset`) исключены из проверки токена.
- CORS настроен для адреса `http://localhost:3000` (можно изменить под свой фронтенд).
- OTP-коды действительны 10 минут, после проверки удаляются из базы.

## Письма

- Приветственное письмо после регистрации
- OTP-письма (верификация и сброс пароля) в фирменном стиле МГТУ (синий цвет, логотип, блок с кодом)
- Шаблоны на Thymeleaf лежат в `src/main/resources/templates/mail/`

## Тестирование через Postman

1. Импортируйте коллекцию Postman (файл лежит в корне репозитория — `postman_collection.json`).
2. Используйте переменные окружения в Postman для автоматической подстановки токена.

### Пример запроса на вход

```http
POST /api/v1/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

В ответе придёт JSON с JWT и кука `jwt`.

### Защищённый запрос (получение профиля)

```http
GET /api/v1/profile
Authorization: Bearer <ваш_токен>
```

## Конфигурация

Основные настройки в `application.yaml`:

- `server.servlet.context-path=/api/v1`
- `spring.jpa.hibernate.ddl-auto=update`
- `spring.mail.*` — настройки SMTP
- `jwt.secret` — секретный ключ для JWT (рекомендуется передавать через переменную окружения `JWT_SECRET`)

## Разработчикам

1. Создавайте ветку для новой функциональности
2. Добавьте необходимые тесты
3. Убедитесь, что сборка проходит успешно (`mvn clean package`)
4. Создайте pull request

---

**Разработано командой МГТУ им. Н.Э. Баумана**
