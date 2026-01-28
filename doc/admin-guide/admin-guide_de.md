# Administrator-Handbuch

Dieses Handbuch ist für Benutzer mit der Rolle `ADMIN` bestimmt. Es deckt die Verwaltungsfunktionen der AngularAI-Anwendung ab.

## Inhaltsverzeichnis
1. [Benutzerverwaltung](#benutzerverwaltung)
2. [Logs](#logs)
3. [Rollen und Berechtigungen](#rollen-und-berechtigungen)

## Benutzerverwaltung
Administratoren können alle Benutzer im System verwalten:
- **Benutzerliste**: Zeigen Sie eine Tabelle aller registrierten Benutzer an.
- **Benutzer bearbeiten**: Aktualisieren Sie Benutzerdetails wie Name, E-Mail und Rolle.
- **Benutzer löschen**: Entfernen Sie Benutzer aus dem System (Vorsicht: Dies ist dauerhaft).
- **Passwort zurücksetzen**: Administratoren können bei Bedarf Passwörter für Benutzer aktualisieren.

## Logs
Auf der Seite Logs können Administratoren die Systemaktivitäten überwachen:
- **Echtzeit-Updates**: Logs werden automatisch aktualisiert, wenn neue Aktionen auftreten.
- **Filterung**: Filtern Sie Logs nach Benutzer, Aktion oder Zeitstempel, um bestimmte Ereignisse zu untersuchen.
- **Fehlerverfolgung**: Überwachen Sie fehlgeschlagene Anmeldeversuche oder Systemfehler.

## Rollen und Berechtigungen
Das System verwendet eine rollenbasierte Zugriffskontrolle (RBAC):
- **USER**: Kann Aufgaben verwalten, das Dashboard und das eigene Profil einsehen.
- **ADMIN**: Hat vollen Zugriff auf alle Funktionen, einschliesslich Benutzerverwaltung und Logs.

### Zuweisen von Rollen
Rollen können während der Benutzerbearbeitung in der Benutzerverwaltungskonsole zugewiesen werden. Änderungen werden wirksam, wenn sich der Benutzer das nächste Mal anmeldet oder seine Sitzung aktualisiert.
