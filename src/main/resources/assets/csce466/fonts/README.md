# Шрифты для Ban Assistant ImGui

Поместите сюда TTF-шрифт с поддержкой кириллицы.

## Рекомендуемый шрифт: Roboto-Regular.ttf

Скачайте его отсюда:
- **Google Fonts**: https://fonts.google.com/download?family=Roboto (выберите Regular 400)
- **GitHub**: https://github.com/google/roboto/raw/main/fonts/Roboto-Regular.ttf

## Альтернативные шрифты (всё с кириллицей):
- **DejaVu Sans** — https://dejavu-fonts.github.io/ (самый надёжный)
- **Liberation Sans** — https://www.fontsquirrel.com/fonts/liberation-sans
- **Noto Sans** — https://fonts.google.com/noto

## Что делать:

1. Скачайте `Roboto-Regular.ttf` (или другой шрифт с кириллицей).
2. Положите файл в эту папку: `src/main/resources/assets/csce466/fonts/Roboto-Regular.ttf`
3. Пересоберите мод: `gradlew clean reobfShadowJar`

## Как это работает:

Код в `ImGuiRenderer.java` ищет файл `assets/csce466/fonts/Roboto-Regular.ttf` в ресурсах мода,
копирует его во временный файл, и загружает в ImGui с явным указанием диапазона кириллицы.

Если файл не найден — мод упадёт на системные шрифты (Windows/Linux/macOS) или встроенный шрифт ImGui (без кириллицы).
