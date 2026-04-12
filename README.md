# FestManager

> Application web de gestion de bénévoles et de logistique pour festivals et événements culturels.

![Status](https://img.shields.io/badge/status-en%20développement-orange)
![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-green)
![Angular](https://img.shields.io/badge/Angular-17-red)
![Docker](https://img.shields.io/badge/Docker-Compose-blue)
![RGPD](https://img.shields.io/badge/RGPD-conforme-green)

---

## Présentation

FestManager est né d'un besoin terrain : gérer efficacement des dizaines de bénévoles sur un festival avec des missions variées, des créneaux horaires, des organisations prestataires (sécurité, catering...) et des accréditations — le tout en temps réel.

L'application remplace les tableurs, emails et WhatsApp par une plateforme centralisée, conforme au RGPD, déployable via Docker en quelques minutes.

### Fonctionnalités principales

- Gestion des événements, missions et créneaux horaires
- Inscription bénévoles via 3 flux (libre, manuelle, invitation email)
- Affectation bénévoles aux créneaux avec contrôle automatique des conflits horaires
- Tableau de bord temps réel via WebSocket
- Gestion des organisations prestataires avec espace référent dédié
- Génération d'accréditations avec QR code
- Conformité RGPD complète (consentement, export, anonymisation, journal d'audit)

---

## Stack technique

| Couche | Technologie |
|---|---|
| Backend | Spring Boot 3 / Java 17 |
| Frontend | Angular 17 + Angular Material |
| Base de données | PostgreSQL 15 |
| Temps réel | WebSocket (STOMP over SockJS) |
| Auth | Spring Security + JWT |
| Containerisation | Docker + Docker Compose |
| CI/CD | GitHub Actions |

---

## Architecture

```
FestManager/
├── backend/          # API REST Spring Boot
├── frontend/         # Application Angular
├── docs/             # Documentation technique
└── docker-compose.yml
```

```
[ Angular Frontend ]
        |
   HTTP REST + WebSocket
        |
[ Spring Boot API ]
        |
   JPA / Hibernate
        |
[ PostgreSQL ]
```

---

## Démarrage rapide

### Prérequis

- Docker Desktop installé
- Git

### Lancer l'application

```bash
git clone https://github.com/<ton-pseudo>/FestManager.git
cd FestManager
docker-compose up --build
```

L'application est accessible sur :
- Frontend : http://localhost:4200
- API backend : http://localhost:8080
- Documentation API (Swagger) : http://localhost:8080/swagger-ui.html

---

## Documentation

La documentation complète du projet est disponible dans le dossier [`/docs`](./docs) :

- [Documentation générale](./docs/FestManager_Documentation.md) — contexte, modèle de données, roadmap

---

## Roadmap

- [x] Cadrage du projet et modèle de données
- [ ] Fondations techniques (Spring Boot + Angular + Docker)
- [ ] Core features (CRUD, affectations, WebSocket)
- [ ] Features avancées (QR code, exports, RGPD)
- [ ] Déploiement démo en ligne

---

## Conformité RGPD

FestManager intègre la conformité RGPD dès sa conception :

- Consentement explicite recueilli et tracé à l'inscription
- Droits des personnes implémentés (accès, rectification, effacement, portabilité)
- Journal d'audit de tous les accès aux données personnelles
- Anonymisation automatique après la durée de conservation définie
- Aucune transmission de données à des tiers sans consentement

---

## Auteur

**Alan** — Développeur Full Stack (Java / Spring Boot / Angular)  
Projet personnel — Portfolio recruteur  

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Profil-blue)](https://linkedin.com)
[![GitHub](https://img.shields.io/badge/GitHub-Profil-black)](https://github.com)
