% Software-Entwicklung mit AI
% AI-gestützte Softwareentwicklung: von Idee zu Code, Tests und Doku
% Jürg Good 13.1.2026

# Agenda
- **Demo & Kontext**
  - Angular Web & Android App
- **AI-Werkzeuge**
  - Junie AI, ChatGPT
- **Praxis**
  - Vorgehensweise & Beispiele
  - AI implementiert Feature
- **Einordnung**
  - Fazit & Ausblick

::: notes
Übersicht über den Ablauf der Präsentation.
Ziel ist es, erst Kontext und Demo zu zeigen,
dann die eingesetzten AI-Werkzeuge zu erklären
und zum Schluss Erfahrungen und eine Einordnung zu geben.
:::

# Anwendungsübersicht

::: columns
::: column
![**Web-UI** www.goodone.ch](files/images/angular_task_menu.png)


:::
::: column
![**Android App**](files/images/android_task_menu.png)

:::
:::

# AI Uebertrifft Erwartungen

::: columns
::: column
![**Geplant** www.goodone.ch](files/images/GoodOne2020_Users.png)

Implementiert 2020 'von Hand'

:::
::: column
![**Resultat**](files/images/dashboard.png)

Implementiert 2026 durch AI
+ Android App 'gratis' dazu

:::
:::

::: notes
Die gleiche Anwendung existiert als Web- und als Android-App.
Beide greifen auf dasselbe Backend zu.
Das ist wichtig, weil AI-Unterstützung über alle Ebenen hinweg genutzt wurde.
:::

# Initiale Aufgabe an AI
- 1. Objectives
  - Create a Spring Boot backend.
  - Create an Angular frontend.
  - Implement a User administration system with:
      - Firstname, lastname, login, password, birthdate, address.
      - Login and profile editing pages.

- 2. Initial Plan
1. Initialize Spring Boot project in `backend` folder.
2. Initialize Angular project in `frontend` folder.
3. Define User entity and repository in Backend.
4. Implement Backend REST API for User administration (CRUD + Login).
5. Implement Frontend services for API interaction.
6. Create Login page in Frontend.
7. Create User Profile Editing page in Frontend.
8. Verify the application functionality.

# Architekturübersicht – Infrastruktur
![](files/generated/architecture_overview.png)


# AI unterstützte Entwicklung
![](files/images/IdeIntegration.png)


# Architekturübersicht – Logisch
- **Clients**
  - Angular Web-UI
  - Android App (Jetpack Compose)
  - Test-Daten-Generator
- **Backend**
  - Java, Spring Boot REST-API
- **Persistenz**
  - PostgreSQL (AWS RDS)
  - Lokal: H2

::: notes
Hier sehen wir die logische Architektur.
Mehrere Clients greifen auf eine gemeinsame REST-API zu.
Die Persistenz ist produktiv PostgreSQL,
lokal wird H2 für Entwicklung und Tests genutzt.
:::

::: notes
Die Anwendung läuft containerisiert in AWS.
Docker ermöglicht reproduzierbare Builds,
ECS Fargate reduziert operativen Aufwand.
Nach dieser Folie folgt die Live-Demo.
:::

# AI Toolset
- **Junie AI (JetBrains)**
  - Code-Generierung, Refactoring, Tests
- **ChatGPT (OpenAI)**
  - UX-Design, Architektur-Reviews, Konzeptarbeit
- **IDE-Integration**
  - IntelliJ IDEA, Android Studio

::: notes
Wir nutzen nicht ein einzelnes Tool,
sondern eine Kombination.
Junie ist stark in der direkten Code-Arbeit in der IDE,
ChatGPT unterstützt eher konzeptionell und bei Reviews.
:::

# Vorgehen bei Design durch AI
1. **Prompting**
   - Ziel und Kontext definieren
2. **Struktur**
   - Entitäten, Attribute, Menüs
3. **Planung**
   - Detaillierter Markdown-Plan
4. **Review**
   - Entwickler prüft & ergänzt
5. **Implementierung**
   - Code + Tests durch AI
6. **Qualität**
   - Entwickler bleibt verantwortlich

::: notes
Wichtig ist ein strukturierter Ablauf.
AI arbeitet nicht „magisch“,
sondern folgt klaren Vorgaben.
Der Entwickler bleibt jederzeit verantwortlich
und prüft die Ergebnisse kritisch.
:::

# Prompting-Beispiele
- Implementiere ... 
- Frontend und Backend mit diesen Anforderungen
- Android App mit identischen Features wie Web App
- Dashboard UI zu diesem Screenshot von ChatGPT
- Read-only Admin Access
- AWS Cloud Deployment
- UI-E3E Integrationstests mit Cypress
- Verbessere UX mit Design-Vorschlag von ChatGPT
- Behebe alle QA Fehler (Sonar)
- Verbessere UI E2E Test Coverage > 80%
- Erstelle Architektur-Diagramm
- Confluence-Dokumentation und User Guide
- Testdaten-Generierung und Daten-Import
- Behebe Defekt XYZ (paste Exception Stacktrace)
- Powerpoint-Präsentation 
- Generiere Slide mit QA KPIs


# Demo
- www.goodone.ch
- Code-Generierung
- UI E2E Tests
- Refactoring
- Dokumentation

# Resultat
- modernes UI Design
- aktuellste Technologien (Angular,SpringBoot,...)
- hohe Code-Qualität
- Automatisierte Tests (UI und Backend)
- Automatisierte Entwicklungsprozesse
- Zeitersparnis 400% im Vergleich zum Projekt 2020
- durch AI aktuelle Technologien kennengelernt

# KI-gestützt mit Junie (IDE-zentriert)
- Direkte IDE-Integration
- Kontext aus Projekt & Code
- Schnelle Iterationen
- Fokus auf Implementierung

::: notes
Junie kennt den Projektkontext,
Dateien, Abhängigkeiten und Tests.
Das macht das Tool besonders effizient
für Implementierung, Refactoring und Tests.
:::

# ChatGPT (extern & beratend)
- Architektur- & UX-Feedback
- Alternative Lösungsansätze
- Formulierung & Dokumentation
- Kein direkter Code-Zugriff

::: notes
ChatGPT wird bewusst extern genutzt.
Es ist ideal für Reviews, Ideen und Formulierungen,
hat aber keinen direkten Zugriff auf den Code.
Das reduziert Risiken im Firmenumfeld.
:::

# Einschränkungen im Firmenumfeld
- **Security & Compliance**
  - Keine Kundendaten extern
- **Tool-Vorgaben**
  - Zentrale AI-Freigaben
- **Technologie-Gap**
  - Verzögerter Zugang zu aktuellen Modellen
- **Know-how**
  - Prompting-Kompetenz nötig

::: notes
Prompting-Kompetenz bedeutet:
AI liefert nur so gute Ergebnisse wie die Aufgabenstellung.
Gute Prompts erfordern Domänenwissen,
technisches Verständnis und klare Ziele.
AI ersetzt kein Know-how – sie verstärkt es.
:::


# Fazit
- **Demo bestätigt**: AI ist ideal für schnelle Prototypen
- **Werkzeuge**: Junie & ChatGPT ergänzen sich ideal
- **Praxis**: Weniger Code, mehr Qualität
- **Ausblick**: AI-gestützte Entwicklung wird Standard

::: notes
Die Demo zeigt, dass AI heute produktiv eingesetzt werden kann.
Entscheidend ist die richtige Kombination von Werkzeugen
und die Einbettung in bestehende Entwicklungsprozesse.
Schneller Feedback Loop
:::

## Schneller bauen. Besser verstehen. Qualität sichern.
### Mit Junie AI & ChatGPT

::: notes
Abschliessende Kernbotschaft:
AI beschleunigt Entwicklung,
verbessert Qualität,
ersetzt aber nicht die Verantwortung des Entwicklers.
:::

# Impression zum Schluss
::: columns
::: column
![](files/images/AiRace.png)
Start

:::
::: column
![](files/images/AiRaceFinal.png)
Etappenziel

:::
:::


# Links

GIT Repo ()
- www.goodone.ch
- https://github.com/JuergGood/angularai.git (öffentlich)
- https://sonarcloud.io/summary/overall?id=JuergGood_angularai&branch=master
- 


# Cypress Report
![](files/generated/cypress_report.png)

# Sonar Report
![](files/generated/sonar_report.png)


# Tech Stack & Stats
| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 4, JPA, Security, Maven |
| **Frontend** | Angular 21, TypeScript, Material, Signals |
| **Mobile** | Android (Jetpack Compose, Kotlin) |
| **DevOps** | Docker, AWS RDS/Fargate, SonarCloud |

| Component | Files | Approx. LOC |
| :--- | :--- | :--- |
| **Backend** | 42 | ~2,400 |
| **Frontend** | 43+ | ~5,300 |
| **Android** | 147 | ~7,900 |
| **Total** | **~240+** | **~16,500+** |

::: notes
Statistics as of Jan 2026.
Backend includes Main and Test code (~1:1 ratio).
Frontend includes TS, HTML, and CSS.
:::

# Quality Assurance (QA) KPIs
| Area | Tooling | Metric                 |
| :--- | :--- |:-----------------------|
| **Backend** | JUnit 5, Mockito, JaCoCo | ~82% Line Coverage     |
| **Frontend** | Vitest, Cypress, NYC | ~81% Line Coverage     |
| **Analysis** | SonarCloud, Qodana | Clean Code / A-Rating  |
| **Automation** | GitHub Actions, Scripts | Automated Build & Test |

::: notes
- **High Coverage**: Both frontend and backend maintain ~80% coverage.
- **Deep Testing**: Backend uses Mockito for isolation; Frontend combines Unit (Vitest) and E2E (Cypress) tests.
- **Continuous Quality**: SonarCloud and Qodana ensure long-term code health.
- **Reproducibility**: Docker-based tests ensure "it works on my machine" translates to CI/CD.
:::

# E2E Tests
![](files/images/CypressTests.png)
- Umfassende E2E UI Tests mit Cypress

