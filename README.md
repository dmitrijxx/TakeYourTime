# Abwesenheitsverwaltung (TakeYourTime)

Eine webbasierte Abwesenheitsverwaltung für Teams/Gruppen.<br>
Dieses Projekt entstand im Rahmen eines Universitätskurses und wurde als Teamprojekt mit drei Personen umgesetzt.<br>
Die Anwendung ermöglicht es, Abwesenheiten wie Urlaube oder Krankheitstage zu erfassen und übersichtlich darzustellen.

## Tech Stack:

- Backend: Spring Boot
- Frontend: Angular
- Datenbank: (SQLite)
- Docker

## Features:

- Erstellung und Verwaltung von Abteilungen und Systemnutzern
- Eintragung von Abwesenheiten (z.B. Urlaub, Sonderurlaub, Krankheit)
- Dashboard mit:
    - Kalenderbasierter Übersicht aller Abwesenheiten innerhalb einer Abteilung
    - Anzahl der genommenen Urlaubstage pro Mitarbeiter
    - Fehlzeitenübersicht

## Getting Started:

Projekt mit Docker starten:

```bash
docker-compose up --build
```
Die Anwendung ist danach erreichbar unter: http://localhost:4200

**Default-User(login)**:
- Username: admin
- Password: admin **(ggf. ändern)**