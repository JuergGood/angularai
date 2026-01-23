# AngularAI-Anwendung

Dies ist eine Full-Stack-Anwendung mit einem Spring Boot-Backend und einem Angular-Frontend.

## Voraussetzungen

- [Docker](https://www.docker.com/get-started)
- [Docker Compose](https://docs.docker.com/compose/install/)

## Ausführen mit Docker

Um die gesamte Anwendung mit Docker Compose auszuführen, navigieren Sie zum Stammverzeichnis. Erstellen Sie zunächst eine `.env`-Datei im Stammverzeichnis (Sie können `.env.example` als Vorlage kopieren):

```bash
cp .env.example .env
```

Führen Sie dann den folgenden Befehl aus:

```bash
docker compose up --build
```

Die Anwendung ist verfügbar unter:
- Frontend: [http://localhost](http://localhost)
- Backend API: [http://localhost:8080/api](http://localhost:8080/api)
- H2-Konsole: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:testdb`)

## Deployment-Skripte

Skripte für gängige Deployment-Aufgaben finden Sie im Ordner `scripts/` (PowerShell und Windows CMD). **Hinweis: PowerShell-Skripte laden Variablen automatisch aus Ihrer lokalen `.env`-Datei.**

- **Lokales Docker-Deployment**: `.\scripts\deploy-local.ps1` oder `.\scripts\deploy-local.bat`
  - Führt `docker compose up --build -d` aus, um die Anwendung lokal im Hintergrund zu starten.
- **AWS-Deployment**: `.\scripts\deploy-aws.ps1` oder `.\scripts\deploy-aws.bat`
  - Authentifiziert sich bei AWS ECR, erstellt, taggt und pusht Frontend- und Backend-Images und erzwingt ein neues Deployment auf ECS-Services.
- **Umgebung laden**: Das Skript `load-env.ps1` wird von anderen PowerShell-Skripten verwendet, um sicherzustellen, dass sensible Schlüssel (wie `IPSTACK_API_KEY`) in der Sitzung verfügbar sind.

## Projektstruktur

- `backend/`: Spring Boot-Anwendung.
- `frontend/`: Angular-Anwendung.
- `android/`: Android Jetpack Compose-Anwendung.
- `docker-compose.yml`: Orchestrierung für beide Services.

## Entwicklung

### IntelliJ IDEA Setup
Um das Backend von IntelliJ auszuführen, müssen Sie sicherstellen, dass die erforderlichen Umgebungsvariablen (wie `IPSTACK_API_KEY`) verfügbar sind.
- Sie können diese manuell zu Ihren Run-Konfigurationen hinzufügen.
- Alternativ können Sie ein Plugin wie **EnvFile** verwenden, um die `.env`-Datei automatisch in Ihre Run-Konfigurationen zu laden. **Übergeben (commit) Sie diese Schlüssel niemals an Git.**

### Frontend (Web)
Navigieren Sie zu `frontend/` und führen Sie `npm install` und dann `npm start` aus.
Der Angular-Entwicklungsserver ist so konfiguriert, dass er `/api`-Anfragen an `http://localhost:8080` weiterleitet. Stellen Sie sicher, dass das Backend läuft.

## Dokumentation

- [Benutzerhandbuch](doc/userguide/user-guide.md)
- [Versionshinweise](doc/userguide/release-notes.md)
- [Administrator-Handbuch](doc/userguide/admin-guide.md)
- [FAQ](doc/userguide/faq.md)
- [Confluence-Exportskript](scripts/md_to_confluence.py)
- [Android-Bauanleitung](doc/ai/android/android-build-instructions.md)
- [AWS-Setup und Infrastruktur](doc/ai/aws/aws_setup.md)
- [PostgreSQL-Setup](doc/ai/backend/postgres_setup.md)
- [ECS Fargate-Konfiguration](doc/ai/aws/aws_fargate_config.md)
- [Erstellen einer Backend-Zielgruppe](doc/ai/aws/aws_create_target_group.md)
- [ALB und Konnektivitäts-Fehlerbehebung](doc/ai/aws/aws_alb_troubleshooting.md)
- [Pushen von Images zu Amazon ECR](doc/ai/aws/aws_ecs_push_instructions.md)
