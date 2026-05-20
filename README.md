# Wish List (Jetpack Compose)

Мобильное приложение для создания и совместного использования вишлистов, бронирования подарков и управления профилем.

## Технологии
- Kotlin, Jetpack Compose, Material 3
- Clean Architecture (`data` / `domain` / `ui`)
- Hilt (DI)
- Firebase: Firestore, FCM, Remote Config
- WorkManager
- XML + ComposeView / AndroidView (гибридная интеграция)

## Запуск
1. Открыть проект в Android Studio.
2. Добавить `google-services.json` и `local.properties` (ключи SDK при необходимости).
3. Убедиться, что Firestore Rules опубликованы.
4. Сборка:
   - `./gradlew :app:assembleDevDebug`
   - `./gradlew :app:assembleProdRelease`

---

## Покрытие критериев семестровой работы (20 баллов)

### Обязательные критерии (15/15)

## 1) Чистая архитектура (5 б)
- Слои и модули:
  - `domain/` (модели, репозитории, use case)
  - `data/` (реализации репозиториев, маппинг, источники данных)
  - `ui/` (Compose UI, ViewModel)
  - `app/` (DI, Activity, Firebase wiring)
- Примеры изолированных Use Case:
  - [CreateWishlistUseCase.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/domain/src/main/kotlin/com/example/wish_list/domain/usecase/wishlist/CreateWishlistUseCase.kt)
  - [ReserveGiftItemUseCase.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/domain/src/main/kotlin/com/example/wish_list/domain/usecase/reservation/ReserveGiftItemUseCase.kt)
- Репозитории и зависимости «внутрь»:
  - Интерфейсы: [domain/repository](/C:/Users/lidiya/AndroidStudioProjects/wish_list/domain/src/main/kotlin/com/example/wish_list/domain/repository)
  - Реализации: [data/repository](/C:/Users/lidiya/AndroidStudioProjects/wish_list/data/src/main/kotlin/com/example/wish_list/data/repository)
- DI (Hilt):
  - [AppBindingsModule.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/src/main/java/com/example/wish_list/di/AppBindingsModule.kt)
  - [AppProvidesModule.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/src/main/java/com/example/wish_list/di/AppProvidesModule.kt)
  - [FirebaseModule.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/src/main/java/com/example/wish_list/di/FirebaseModule.kt)

## 2) Фоновые задачи и сервисы (3 б)
- WorkManager (периодическая задача + constraints):
  - [RemoteConfigSyncWorker.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/src/main/java/com/example/wish_list/work/RemoteConfigSyncWorker.kt)
  - планирование в [WishListApplication.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/src/main/java/com/example/wish_list/WishListApplication.kt)
- Service:
  - [PushMessagingService.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/src/main/java/com/example/wish_list/firebase/PushMessagingService.kt)

## 3) Анимации в Jetpack Compose (2 б)
- `AnimatedVisibility` для загрузки/состояний.
- `AnimatedContent` для переходов между экранами.
- Дополнительно: `fadeIn/fadeOut`, `scaleIn/scaleOut`.
- Реализация: [WishlistApp.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/ui/src/main/java/com/example/wish_list/ui/WishlistApp.kt)

## 4) XML + Compose интеграция (2 б)
- Экран на XML с `ComposeView`:
  - [activity_hybrid_compose.xml](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/src/main/res/layout/activity_hybrid_compose.xml)
  - [HybridComposeActivity.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/src/main/java/com/example/wish_list/HybridComposeActivity.kt)
- Внутри Compose используется `AndroidView` (Yandex MapView) и данные из ViewModel.

## 5) Gradle конфигурация сборок (2 б)
- `buildTypes` (`debug`/`release`) + `productFlavors` (`dev`/`prod`).
- Отличия по суффиксам/конфигурации и release-настройкам (R8/ProGuard).
- Основной файл: [app/build.gradle.kts](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/build.gradle.kts)

## 6) Качество кода и UX (1 б)
- Обработка ошибок/сообщений состояния в ViewModel.
- Экранные состояния: loading/content/errors.
- Разделение ответственности по слоям и use case.
- Пример: [WishlistViewModel.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/ui/src/main/java/com/example/wish_list/ui/WishlistViewModel.kt)

---

### Бонусные критерии

## Firebase (+2 б)
Реализованы все 3 из 3:
1. Push-уведомления (FCM):
   - [PushMessagingService.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/src/main/java/com/example/wish_list/firebase/PushMessagingService.kt)
2. Remote Config:
   - [FirebaseRemoteConfigService.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/src/main/java/com/example/wish_list/firebase/FirebaseRemoteConfigService.kt)
3. Firestore как основное хранилище бизнес-данных:
   - [FirestoreWishlistRepository.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/data/src/main/kotlin/com/example/wish_list/data/repository/FirestoreWishlistRepository.kt)
   - [FirestoreGiftItemRepository.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/data/src/main/kotlin/com/example/wish_list/data/repository/FirestoreGiftItemRepository.kt)
   - [FirestoreReservationRepository.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/data/src/main/kotlin/com/example/wish_list/data/repository/FirestoreReservationRepository.kt)

## Интеграция внешнего сервиса / аналитики (+1 б)
- Авторизация через VK и Yandex:
  - [ExternalAuthService.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/src/main/java/com/example/wish_list/auth/ExternalAuthService.kt)
- AppMetrica аналитика с событиями:
  - [AppMetricaAnalyticsService.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/src/main/java/com/example/wish_list/AppMetricaAnalyticsService.kt)

## Использование ИИ (+0 б )
- AI-сценарий в текущей версии приложения не реализован.

---

## Дополнительно для проверки на защите
- Основной UI и навигация: [WishlistApp.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/ui/src/main/java/com/example/wish_list/ui/WishlistApp.kt)
- Главная точка входа и wiring: [MainActivity.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/src/main/java/com/example/wish_list/MainActivity.kt)
- Приложение и инициализация: [WishListApplication.kt](/C:/Users/lidiya/AndroidStudioProjects/wish_list/app/src/main/java/com/example/wish_list/WishListApplication.kt)

## Примечание по Firestore Rules
Для корректной работы чтения/записи должны быть опубликованы правила Firestore, согласованные с текущей моделью идентификаторов (`firebase uid` + `activeExternalUserId`).
