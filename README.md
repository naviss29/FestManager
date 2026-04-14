# FestManager

> Application web de gestion de bénévoles et de logistique pour festivals et événements culturels.

![Status](https://img.shields.io/badge/status-Phase%204%20en%20cours-blue)
![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-green)
![Angular](https://img.shields.io/badge/Angular-17-red)
![Docker](https://img.shields.io/badge/Docker-Compose-blue)
![Tests](https://img.shields.io/badge/tests-20%20passing-brightgreen)
![RGPD](https://img.shields.io/badge/RGPD-conforme-green)

---

## Présentation

FestManager est né d'un besoin terrain : gérer efficacement des dizaines de bénévoles sur un festival avec des missions variées, des créneaux horaires, des organisations prestataires et des accréditations — le tout en temps réel.

L'application remplace les tableurs, emails et WhatsApp par une plateforme centralisée, conforme au RGPD, déployable via Docker en quelques minutes.

---

## Fonctionnalités

| Fonctionnalité | Statut |
|---|---|
| Authentification JWT (login + inscription) | ✅ |
| Gestion des événements (CRUD) | ✅ |
| Gestion des missions et créneaux | ✅ |
| Inscription bénévoles (3 flux : libre, manuelle, invitation email) | ✅ |
| Affectation bénévoles avec contrôle des conflits horaires | ✅ |
| Tableau de bord temps réel (WebSocket STOMP) | ✅ |
| Gestion des organisations prestataires + espace référent | ✅ |
| Journal d'audit RGPD | ✅ |
| Accréditations avec génération de QR code | ✅ |
| Export planning CSV et PDF | ✅ |
| Notifications email (confirmation affectation, invitation, rappel) | ✅ |
| Anonymisation automatique RGPD (job nocturne, Art. 17) | ✅ |
| Page Mentions Légales | ✅ |
| Documentation API Swagger | ✅ |
| Déploiement démo en ligne (Railway) | 🔲 |

---

## Stack technique

| Couche | Technologie |
|---|---|
| Backend | Spring Boot 3 / Java 17 |
| Frontend | Angular 17 + Angular Material |
| Base de données | PostgreSQL 15 |
| Temps réel | WebSocket (STOMP over SockJS) |
| Auth | Spring Security + JWT |
| QR Code | ZXing 3.5.3 |
| Export PDF | OpenPDF 2.0.3 |
| Export CSV | Apache Commons CSV 1.11 |
| Email | Spring Boot Mail (SMTP) |
| Containerisation | Docker + Docker Compose |
| CI/CD | GitHub Actions |

---

## Architecture

```
FestManager/
├── backend/           # API REST Spring Boot
├── frontend/          # Application Angular
├── docs/              # Documentation technique
└── docker-compose.yml
```

```
[ Angular Frontend (nginx) ]
          |
    /api/* et /ws/* (proxy nginx)
          |
[ Spring Boot API :8080 ]
          |
  JPA / Hibernate + WebSocket STOMP
          |
[ PostgreSQL :5432 ]
```

---

## Lancement

### Avec Docker *(recommandé)*

```bash
git clone https://github.com/naviss29/FestManager.git
cd FestManager
docker compose build
docker compose up
```

| Service | URL |
|---|---|
| Application | http://localhost:4200 |
| API backend | http://localhost:8080 |

> Docker Desktop doit être démarré. Seuls Docker et Git sont nécessaires.

### En développement local *(sans Docker)*

Le profil `dev` utilise une base H2 embarquée — aucune installation requise.

```bash
# Backend
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend (dans un autre terminal)
cd frontend
npm install && npm start
```

---

## Tests

```bash
cd backend
./mvnw test
```

20 tests unitaires (Mockito, sans Spring context) couvrant les services de la Phase 3 :
QR code, accréditations, export CSV/PDF, job RGPD, email.

---

## Documentation

- [Documentation complète](./docs/FestManager_Documentation.md) — contexte, modèle de données, RGPD, roadmap
- [Pense-bête / idées futures](./docs/pense-bete.md)

---

## Roadmap

- [x] Phase 0 — Cadrage, modèle de données, documentation
- [x] Phase 1 — Fondations (Spring Boot, Angular, Docker, JWT, entités JPA)
- [x] Phase 2 — Core features (CRUD complet, affectations, WebSocket, auth)
- [x] Phase 3 — Features avancées (QR codes, dashboard temps réel, exports CSV/PDF, RGPD, email, mentions légales)
- [ ] Phase 4 — Finalisation (Swagger, déploiement Railway, README screenshots)

---

## Conformité RGPD

FestManager intègre la conformité RGPD dès sa conception :

- Consentement explicite recueilli et tracé à l'inscription
- Droits des personnes implémentés : accès (Art. 15), rectification (Art. 16), effacement (Art. 17), portabilité (Art. 20)
- Journal d'audit de tous les accès aux données personnelles
- Anonymisation automatique déclenchée chaque nuit (3 ans après le dernier événement)
- Aucune transmission de données à des tiers sans consentement

---

## Auteur

**Alan** — Développeur Full Stack (Java / Spring Boot / Angular)  
Projet personnel — Portfolio recruteur

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Profil-blue)](https://linkedin.com)
[![GitHub](https://img.shields.io/badge/GitHub-naviss29-black)](https://github.com/naviss29)
