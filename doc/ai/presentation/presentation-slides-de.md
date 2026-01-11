---
# Folie 1: Titel
layout: title
subtitle: Beschleunigung der Full-Stack-Entwicklung mit Junie AI

---
# Agenda
layout: agenda
content: |
  - Punkt 1
  - Punkt 2
  - Punkt 3

---
# Folie 2: Anwendungsübersicht
layout: image_right
left: |
  - Einheitliches Erlebnis: Nahtloses Aufgabenmanagement.
  - Web-App: Modernes Angular-Dashboard.
  - Android-App: Natives Erlebnis.
right:
  image: files/images/angular_task_menu.png
  caption: Angular Material Login-Bildschirm

---
# Folie 2b: Android-UI
layout: image_right
left: |
  - Android-App: Natives Erlebnis.
    - Generiert aus Angular Material.
right:
    image: files/images/android_task_menu.png
    caption: Angular Material Login-Bildschirm

---
# Folie 3: Architekturübersicht
layout: title_and_content
content: |
  - Client-Ebene: Angular & Jetpack Compose.
  - API-Ebene: Spring Boot REST-API.
  - Persistenz: PostgreSQL (AWS RDS).
  - Infrastruktur: Containerisiert auf AWS ECS Fargate.
image: files/generated/architecture_overview.png

---
# Folie 4: Datenbankschema (ER-Diagramm)
layout: title_and_content
content: |
  - Automatisierte Schema-Generierung.
  - Entitäten: User, Task, Priority, TaskStatus.
  - Beziehungen: User (1) zu Task (N).
  - Migration: Verwaltet über Flyway.
image: files/generated/erd.png

---
# Folie 5: Lokales Entwicklungs-Setup
layout: image_right
left: |
  - **IntelliJ IDEA + Junie**: Backend & Frontend Kern.
  - **Android Studio + Gemini**: Native mobile Entwicklung.
  - **Docker unter Windows**: Lokales PostgreSQL.
right:
  image: files/generated/local_dev_setup.png
  caption: Integrierte Entwicklungsumgebung

---
# Folie 6: Backend-Exzellenz
layout: title_and_content
content: |
  - Spring Boot 4 & JPA.
  - Clean Code: DTO-basierte Kommunikation.
  - Sicherheit: Rollenbasierte Zugriffskontrolle.
  - Junies Rolle: Scaffolding und Abfrageoptimierung.

---
# Folie 7: Modernes Frontend
layout: title_and_content
content: |
  - Angular 21+ mit Signals.
  - Standalone-Komponenten.
  - Material 3 Design.
  - Junies Rolle: Schnelles UI-Scaffolding.

---
# Folie 8: KI-gesteuerte Entwicklung mit Junie
layout: title_and_content
content: |
  - Kontextbewusstsein über den gesamten Stack.
  - Unit-Test-Generierung (JUnit, Vitest).
  - Befehlsausführung aus der IDE.
  - 40 % Produktivitätssteigerung.

---
# Folie 9: Fallstudie: DB-Entwicklung
layout: title_and_content
content: |
  - Problem: Synchronisierung des Schemas zwischen Backend und Mobile.
  - Prozess: Entität ändern -> Junie generiert Flyway-SQL.
  - Synchronisierung: Junie aktualisiert Android Room-Skripte.
  - Ergebnis: Reibungslose Schema-Updates.

---
# Folie 9c: Two content sample
layout: two_content
left: |
  - Backend & Frontend Kern.
  - Native mobile Entwicklung.

right:
- Lokales PostgreSQL.

---
# Folie 10: Fazit
layout: title_and_content
subtitle: Intelligenter bauen, nicht härter, mit Junie AI

