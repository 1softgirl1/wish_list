# Wish List (Лабораторная работа)

Учебный Android-проект на Kotlin + Jetpack Compose для демонстрации многомодульной архитектуры.

## 1. Краткое описание проекта

`Wish List` — приложение для ведения личных wishlist’ов.

Основной MVP-сценарий:
- пользователь создаёт свои wishlist’ы;
- добавляет в них подарки;
- делится списком по ссылке (share code);
- другой пользователь открывает публичный список;
- резервирует подарок;
- позже отмечает подарок как подаренный.

Проект делался как лабораторная с фокусом на архитектуру, а не на внешний API/бэкенд.

## 2. Что реализовано в MVP

### Экраны
- “Мои wishlist’ы”
- “Детали wishlist”
- “Добавление/редактирование подарка”
- “Публичный просмотр по ссылке”
- “Мои резервы”

### Бизнес-правила
- нельзя зарезервировать уже занятый подарок;
- нельзя пометить подарок как подаренный без активного резерва;
- резерв и статус подарка синхронизируются (`AVAILABLE -> RESERVED -> GIFTED`);
- архивные/неактуальные подарки не участвуют в резервировании.

### Доменные сущности
- `User`
- `Wishlist`
- `GiftItem`
- `Reservation`

### Use case’ы MVP
- `GetMyWishlistsUseCase`
- `CreateWishlistUseCase`
- `GetWishlistDetailsUseCase`
- `AddGiftItemUseCase`
- `UpdateGiftItemUseCase`
- `GetGiftItemsForWishlistUseCase`
- `GetWishlistByShareCodeUseCase`
- `ReserveGiftItemUseCase`
- `CancelReservationUseCase`
- `MarkGiftAsGiftedUseCase`
- `GetMyReservationsUseCase`

## 3. Архитектура и разделение ответственности

### Слои
- `domain`  
  Чистая бизнес-логика: модели, use case’ы, интерфейсы репозиториев.  
  Без Android framework и без деталей хранения.

- `data`  
  Реализация доступа к данным: локальные источники, entity, mapper’ы, реализации репозиториев.

- `ui`  
  Экраны Compose, `ViewModel`, `UiState`, обработка пользовательских действий.

### Базовые модули
- `:app` — точка входа и сборка приложения.
- `:core` — общие утилиты, константы, базовые классы.
- `:core:navigation` — общие навигационные контракты/компоненты.

## 4. Ветки и что в них находится

### `layer-based`
Ветка с “слоистой” декомпозицией:
- акцент на разделение по слоям (`data/domain/ui`) в отдельные модули;
- фичи не являются основой модульного деления;
- удобно показывать классический Clean Architecture подход.

### `feature-based`
Ветка с декомпозицией по фичам:
- `:feature:wishlist`
- `:feature:public-wishlist`
- `:feature:reservation`
- плюс общие `:core` и `:core:navigation`.

Внутри feature-модулей находится их собственный код (UI + domain + data конкретной фичи).

### `combined`
Ветка с комбинированным подходом (Google-style):
- фича разбивается на подмодули слоёв:
  - `:feature:<name>:api`
  - `:feature:<name>:domain`
  - `:feature:<name>:data`
  - `:feature:<name>:ui`
- взаимодействие между фичами идёт через `api`-модули;
- даёт более строгие границы зависимостей и лучше масштабируется.

## 5. Архитектурные проверки (Konsist)

В проект добавлены архитектурные unit-тесты на библиотеке Konsist:
- `domain` не зависит от Android framework;
- `data` не зависит от UI-слоя;
- feature-модули не должны зависеть друг от друга напрямую;
- use case’ы должны находиться в `domain.usecase`;
- репозитории объявляются интерфейсами в `domain.repository`, а реализации находятся в `data.repository`.

Запуск тестов:

```bash
./gradlew :app:testDebugUnitTest
```

## 6. Как переключаться между вариантами архитектуры

```bash
git checkout layer-based
git checkout feature-based
git checkout combined
```

## 7. Примечание

Текущая структура файлов зависит от активной ветки.  
Для проверки конкретного архитектурного подхода переключайтесь на нужную ветку.
