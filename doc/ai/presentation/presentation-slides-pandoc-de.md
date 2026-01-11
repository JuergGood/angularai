% Full-Stack-Entwicklung mit AI
% Jürg Good
% 11. Januar 2026

# Agenda

- Applikations-Demo (Angular, Android)
- AI-Werkzeuge
- Fazit

# Anwendungsübersicht

:::::::::::::: {.columns}
::: {.column width="50%"}
- Angular UI
  - htts://www.goodone.ch
    ![](files/images/angular_task_menu.png)
    :::
    ::: {.column width="50%"}
  - Android App
    ![](files/images/android_task_menu.png)
    - Direkt-Installation von APK htts://www.goodone.ch/android

# Folie 3: Architekturübersicht

- Client 1: Angular UI
- Client 2: Android App mit Jetpack Compose
- Client 3: Test-Daten-Generator. Konsole App mit Kotlin
- API-Ebene: Java, Spring Boot REST-API
- Persistenz: PostgreSQL (AWS RDS). Lokal: H2
- Infrastruktur: Docker Container auf AWS ECS Fargate.

![](files/generated/architecture_overview.png)

::: notes
Gefolgt von Online Demo

# AI Toolset

:::::::::::::: {.columns}
::: {.column width="50%"}
- **IntelliJ IDEA + Junie AI**: Backend & Frontend Kern.
- **Android Studio + Junie & Gemini AI**: Native mobile Entwicklung.
- **Chat GPT 5.2**: UX Design. Aktuell nicht in IntellJ integriert
:::
::: {.column width="50%"}
  ![](files/images\IdeIntegration.png)

*Integrierte Entwicklungsumgebung*
:::
::::::::::::::

# Vorgehen bei Design durch AI

- Fast Prototyping. 
- Auftrag im AI Fenster eingeben: "Entwickle eine Applikation mit Angular Frontent und Spring Boot Backend".
  - Zur Verwaltung von Benutzern mit folgenden Attributen ... Menus im UI ....
- AI entwickelt einen Vorgehens-Plan. (md-Datei)
- Entwickler kontrolliert den Plan. AI ergänzt bei Bedarf den Plan.
- AI generiert Code
- AI Tests aus.
- Entwicker befasst sich mit Plan und Kontrolle des Resultats
- Keine einizge Zeile von Hand geschriebener Code.

# KI-gesteuerte Entwicklung mit Junie

- Junie findent und modifiziert alle Projekt-Dateien
- Unit-Test-Generierung (JUnit, Vitest).
- Befehlsausführung aus der IDE: Build, Tests
- Enorme Produktivitätssteigerung.

# Chat GPT

- Unschlabar bei UX Design.
- Macht kontinuierlich Verbesserungs-Vorschläge
- Inhaltlicher Review. Z.B. diese Präsentation.
- Nachteil: Nicht in IDE integriert. Alle Dateien müssen manuell dem Chat hinzugefügt werden. Dadurch mühsamer.


# Datenbankschema (ER-Diagramm)

- AutomatisierteGenerierung.
- Schema-Design durch Junie.
- Entitäten: User, Task, Priority, TaskStatus.
- Migration: Verwaltet über Flyway.

![](files/generated/erd.png)

# DB-Entwicklung für Android

- Problem: Synchronisierung des Schemas zwischen Backend und Mobile.
- Prozess: Entität ändern -> Junie generiert Flyway-SQL.
- Synchronisierung: Junie aktualisiert Android Room-Skripte.
- Ergebnis: Reibungslose Schema-Updates.

# Einschränkungen bei der ZKB
- AI Tools in ZKB sehr eingschränkt aus Sicherheitsgründen.
- Junie AI kann nicht verwendet werden, 
- Der direkte Code Zugriff würde viel Zeit sparen
- Veraltetes Modell Chat GPT 4.x 

# Fazit
- Robuste Applikationen, da vollständig Unit-getestet
- UI Testing durch 
- Schnelle, inkrementelle Änderungen durch Junie
- Plan treffergenau umgesetzt
- ich bin begeistert


## Schnelle Softwareentwicklung mit Junie AI & Chat GPT. Absoluter Game-Changer.
