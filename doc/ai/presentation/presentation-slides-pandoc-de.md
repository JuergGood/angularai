% Software Development mit AI
%
%

# Agenda
- **Demo & Kontext**
  - Angular Web & Android App
- **AI-Werkzeuge**
  - Junie AI, ChatGPT
- **Praxis**
  - Vorgehensweise & Beispiele
- **Einordnung**
  - Fazit & Ausblick

# Anwendungsübersicht

::: columns
::: column
![](files/images/angular_task_menu.png)
- **Angular Web-UI**
  - https://www.goodone.ch
:::

::: column
![](files/images/android_task_menu.png)
- **Android App**
  - APK Download: https://www.goodone.ch/android
:::
:::

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

# Architekturübersicht – Infrastruktur
![](files/generated/architecture_overview.png)

- Docker Container
- AWS ECS Fargate

::: notes
Gefolgt von Online-Demo
:::

# AI Toolset
- **Junie AI (JetBrains)**
  - Code-Generierung, Refactoring, Tests
- **ChatGPT (OpenAI)**
  - UX-Design, Architektur-Reviews, Konzeptarbeit
- **IDE-Integration**
  - IntelliJ IDEA, Android Studio

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

# KI-gestützte Entwicklung mit Junie (IDE-zentriert)
- Direkte IDE-Integration
- Kontext aus Projekt & Code
- Schnelle Iterationen
- Fokus auf Implementierung

# ChatGPT (extern & beratend)
- Architektur- & UX-Feedback
- Alternative Lösungsansätze
- Formulierung & Dokumentation
- Kein direkter Code-Zugriff

# Einschränkungen im Firmenumfeld
- **Security & Compliance**
  - Keine Kundendaten extern
- **Tool-Vorgaben**
  - Zentrale AI-Freigaben
- **Technologie-Gap**
  - Verzögerter Zugang zu aktuellen Modellen
- **Know-how**
  - Prompting-Kompetenz nötig

# Fazit
- **Demo bestätigt**: AI ist produktionsreif
- **Werkzeuge**: Junie & ChatGPT ergänzen sich ideal
- **Praxis**: Weniger Code, mehr Qualität
- **Ausblick**: AI-gestützte Entwicklung wird Standard

## Schneller bauen. Besser verstehen. Qualität sichern.
### Mit Junie AI & ChatGPT
