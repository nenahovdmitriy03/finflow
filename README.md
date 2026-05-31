# FinFlow 💸

> Красивое, интуитивно понятное Android-приложение для управления личными финансами.

**Stack:** Kotlin · Jetpack Compose · MVVM + Clean Architecture · Hilt · Room · Navigation Compose

## 📋 Документация

Полное ТЗ — см. [FINFLOW_SPEC.md](./FINFLOW_SPEC.md).

## 🎨 Дизайн-система

| Роль | Цвет |
|---|---|
| Primary | `#6C63FF` |
| Income / Success | `#2ECC71` |
| Expense / Warning | `#FF6B6B` |
| Light BG | `#F8F9FE` |
| Dark BG | `#1A1A2E` |

## 🏗 Структура проекта

```
app/src/main/java/com/finflow/
├── data/local/entity/    # Room entities
├── data/local/           # Database
├── domain/model/         # Domain models
├── presentation/ui/      # Compose screens (dashboard, analytics, goals, profile)
├── presentation/theme/   # Compose theme + palette
├── presentation/navigation/
└── di/                   # Hilt modules
```

## 🚀 MVP roadmap

- [x] Архитектурный скелет
- [x] Тема + палитра
- [x] NavGraph (4 вкладки)
- [x] Room entities (Transaction, Category, Wallet, Goal)
- [ ] DAO + Repository
- [ ] Dashboard UI
- [ ] Add Transaction UI
- [ ] Analytics charts
- [ ] Goals tracking

## 🛠 Сборка

```bash
./gradlew assembleDebug
```

Требования: Android Studio Iguana+, JDK 17, Android SDK 35.

## 📄 Лицензия

MIT
