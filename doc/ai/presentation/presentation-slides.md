% Software-Entwicklung mit AI
% AI-gestützte Softwareentwicklung: von Idee zu Code, Tests und Doku
% Jürg Good 19.1.2026

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
![**Web-UI** www.goodone.ch](files/images/DashboardImplementation.png)

:::
::: column
![**Android App**](files/images/AndroidTaskMenu.png)

:::
:::

# Erwartungen übertroffen

::: columns
::: column
![**Geplant:** Vorlage von App 2020](files/images/GoodOne2020_Users.png)

:::
::: column
![**Resultat:** Implementiert mit AI, Android App 'gratis' dazu](files/images/DashboardImplementation.png)

:::
:::

::: notes
Die gleiche Anwendung existiert als Web- und als Android-App.
Beide greifen auf dasselbe Backend zu.
Das ist wichtig, weil AI-Unterstützung über alle Ebenen hinweg genutzt wurde.
:::

# Initiale Aufgabe an die KI

- Alles begann mit diesen Zielen
    - Erstelle ein Spring Boot Backend.
    - Erstelle ein Angular Frontend.
    - Implementiere eine Benutzerverwaltung mit:
        - Vorname, Nachname, Login, Passwort, Geburtsdatum, Adresse.
        - Login- und Profil-Bearbeitungsseiten.

- Initialer Plan, generiert von der KI
    - Spring Boot Projekt im Ordner `backend` initialisieren.
    - Angular Projekt im Ordner `frontend` initialisieren.
    - User-Entität und Repository im Backend definieren.
    - Backend REST-API für die Benutzerverwaltung implementieren (CRUD + Login).
    - Frontend-Services für die API-Interaktion implementieren.
    - Login-Seite im Frontend erstellen.
    - Seite zur Profilbearbeitung im Frontend erstellen.

# Demo

- www.goodone.ch
- Code-Generierung
- UI E2E Tests
- Refactoring
- Dokumentation

# Architekturübersicht – Infrastruktur

![](files/generated/architecture_overview.png)

::: notes
Die Anwendung läuft containerisiert in AWS.
Docker ermöglicht reproduzierbare Builds,
ECS Fargate reduziert operativen Aufwand.
Nach dieser Folie folgt die Live-Demo.
:::

# AI unterstützte Entwicklung

![](files/images/IdeIntegration.png)

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
    - Frontend und Backend mit initialen Anforderungen
    - Android App mit identischen Features wie Web App
    - Dashboard UI gemäss Screenshot von ChatGPT
    - Behebe alle QA-Beanstandungen (Sonar)
    - AWS Cloud Deployment
    - Behebe Defekt XYZ (inkl. Exception Stacktrace)
- Testing
    - E2E Integrationstests mit Cypress
    - Verbessere UI E2E Test Coverage > 80%
    - Testdaten-Generierung und Daten-Import
- Dokumentation
    - Erstelle Architektur-Diagramm
    - Confluence-Dokumentation und User Guide
    - Powerpoint-Präsentation
    - QA KPIs

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

# UX-Verbesserung durch ChatGPT

::: columns
::: column
![Ausgangslage](files/images/DashboardBefore.png)

:::
::: column
![Design Vorschlag](files/images/DashboardProposal.png)

:::
::::

# Resultat

- modernes UI Design
- aktuellste Technologien (Angular,SpringBoot,...)
- hohe Code-Qualität
- Automatisierte Tests (UI und Backend)
- Automatisierte Entwicklungsprozesse
- Zeitersparnis x4 im Vergleich zum Projekt 2020
- durch AI aktuelle Technologien kennengelernt

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
![Start](files/images/AiRace.png)

:::
::: column
![Etappenziel](files/images/AiRaceFinal.png)

:::
:::

# Fragen

::: notes
Ende der Präsentation.
:::

# Anhang

# Links

- <https://www.goodone.ch>
- [https://github.com/JuergGood/angularai.git](https://github.com/JuergGood/angularai.git)(öffentlich)
- [SonarCloud Summary](https://sonarcloud.io/summary/overall?id=JuergGood_angularai&branch=master)

# Architekturübersicht – Logisch

- **Clients**
    - Angular Web-UI
    - Android App (Jetpack Compose)
    - Testdaten-Generator
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

# Cypress Report

![E2E Tests mit Cypress](files/images/CypressTests.png)

# Sonar Report

![Clean Code, A-Rating](files/images/SonarSummary.png)

# Tech Stack & Statistik

::: columns
::: column
| Ebene | Technologien |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 4, JPA, Security, Maven |
| **Frontend** | Angular 21, TypeScript, Material, Signals |
| **Mobile** | Android (Jetpack Compose, Kotlin) |
| **DevOps** | Docker, AWS RDS/Fargate, SonarCloud |
:::

::: column
| Komponente | Dateien | Ca. Zeilen Code (LOC) |
| :--- | :--- | :--- |
| **Backend** | 42 | ~2'400 |
| **Frontend** | 43+ | ~5'300 |
| **Android** | 147 | ~7'900 |
| **Total** | **~240+** | **~16'500+** |
:::
:::

::: notes
Statistik Stand Januar 2026.
Backend enthält Main- und Test-Code (Verhältnis ~1:1).
Frontend enthält TS, HTML und CSS.
:::

# Qualitätssicherung (QA) KPIs

| Bereich             | Werkzeuge                | Metrik                       |
|:--------------------|:-------------------------|:-----------------------------|
| **Backend**         | JUnit 5, Mockito, JaCoCo | ~82% Zeilen-Abdeckung        |
| **Frontend**        | Vitest, Cypress, NYC     | ~81% Zeilen-Abdeckung        |
| **Analyse**         | SonarCloud, Qodana       | Clean Code / A-Rating        |
| **Automatisierung** | GitHub Actions, Scripte  | Automatisierter Build & Test |

::: notes

- **Hohe Abdeckung**: Sowohl Frontend wie auch Backend halten ~80% Abdeckung.
- **Tiefgehende Tests**: Backend nutzt Mockito zur Isolation; Frontend kombiniert Unit- (Vitest) und E2E-Tests (
  Cypress).
- **Kontinuierliche Qualität**: SonarCloud und Qodana sichern die langfristige Code-Gesundheit.
- **Reproduzierbarkeit**: Docker-basierte Tests garantieren, dass "es funktioniert auf meinem Rechner" auch für CI/CD
  gilt.
  :::

# E2E Tests

![Umfassende E2E UI Tests mit Cypress](files/images/CypressTests.png)


