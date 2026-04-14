# FestManager — Documentation du projet

> Version : 0.5  
> Auteur : Alan  
> Date : Avril 2026  
> Statut : **Phase 3 complète — Phase 4 en cours**

---

## Historique des versions

| Version | Date | Modifications |
|---|---|---|
| 0.1 | Avril 2026 | Document initial |
| 0.2 | Avril 2026 | Ajout organisations prestataires + conformité RGPD |
| 0.3 | Avril 2026 | Phases 1 et 2 complétées — backend + frontend fonctionnels |
| 0.4 | Avril 2026 | Dockerisation complète — ajout section lancement et prérequis |
| 0.5 | Avril 2026 | Phase 3 complète — QR codes, dashboard, exports, RGPD, email, mentions légales |

---

## 1. Présentation du projet

### Contexte

FestManager est une application web de gestion de bénévoles et de logistique pour les associations événementielles organisant des festivals, concerts et événements culturels (type HellFest, Motocultor, etc.).

Ce projet est né d'un besoin terrain identifié par son créateur, président d'une association événementielle, confronté quotidiennement à la complexité de gérer des dizaines de bénévoles sur des missions variées avec des outils inadaptés (tableurs, emails, WhatsApp).

### Objectifs

- Offrir un outil de gestion centralisé, simple et temps réel pour les organisateurs
- Permettre aux bénévoles de s'inscrire, consulter leurs missions et créneaux
- Gérer des organisations prestataires (associations partenaires, entreprises de sécurité, etc.)
- Générer les accréditations et badges de manière automatisée
- Respecter pleinement le RGPD et les réglementations en vigueur sur les données personnelles
- Servir de projet portfolio démontrant des compétences Full Stack (Spring Boot + Angular)

### Public cible

- Associations organisant des festivals ou événements (50 à 500 bénévoles)
- Organisateurs et responsables bénévoles
- Organisations prestataires (entreprises de sécurité, associations partenaires)
- Bénévoles eux-mêmes (interface dédiée)

---

## 2. Stack technique

| Couche | Technologie | Justification |
|---|---|---|
| Backend | Spring Boot 3 / Java 17 | Très demandé à Orléans, robuste pour les APIs REST |
| Frontend | Angular 17+ avec Angular Material | Demandé localement, adapté aux apps métier |
| Base de données | PostgreSQL | Open source, fiable, compatible JPA/Hibernate |
| Temps réel | WebSocket (STOMP over SockJS) | Mise à jour live des affectations sans polling |
| Auth | Spring Security + JWT | Standard industriel |
| Containerisation | Docker + Docker Compose | Déploiement simplifié, démo facilement reproductible |
| CI/CD | GitHub Actions | Intégration native GitHub, gratuit pour projets publics |
| Hébergement démo | Railway ou Render | Gratuit pour démo recruteur |
| Tests backend | JUnit 5 + Mockito | Standard Spring Boot |
| Tests frontend | Jasmine + Karma | Standard Angular |
| Audit RGPD | Logs applicatifs + table d'audit JPA | Traçabilité des accès aux données personnelles |

---

## 3. Modèle de données

### Règles métier validées

- Un bénévole peut intervenir sur **plusieurs missions** dans un même événement, **selon son rôle**
- Chaque mission est découpée en **créneaux horaires** (début/fin)
- Un bénévole ne peut pas être affecté à deux missions dont les créneaux se chevauchent
- Le flux d'inscription bénévole est **triple** : inscription libre, création manuelle par l'orga, ou invitation par email
- Les rôles métier identifiés : Roadie/Technique scène, Accueil/Billetterie, Sécurité, Catering, Communication/Photo, Logistique générale
- Une organisation peut **prendre en charge une mission entière** (ex : sécurité confiée à une entreprise) **et/ou fournir des bénévoles individuels** selon les cas
- Une organisation prestataire a un **accès limité** : elle peut consulter ses missions et valider ses propres membres, mais ne gère pas l'événement

### Règles RGPD intégrées au modèle

- Toutes les données personnelles (nom, prénom, email, téléphone) sont **isolées dans l'entité BENEVOLE**
- Un bénévole peut exercer son **droit à l'effacement** : ses données sont anonymisées, ses affectations historiques sont conservées sous forme anonyme
- Un bénévole peut exercer son **droit d'accès** : export de toutes ses données en JSON
- Le **consentement explicite** est recueilli à l'inscription et tracé en base (date + version des CGU)
- Toute consultation de données personnelles par un utilisateur applicatif est **tracée dans un journal d'audit**
- Les données ne sont **jamais transmises à des tiers** sans consentement explicite
- **Durée de conservation** : données bénévoles supprimées ou anonymisées 3 ans après le dernier événement

### Entités principales

#### UTILISATEUR
Représente un compte applicatif (organisateur, admin ou référent d'organisation).

| Champ | Type | Contrainte |
|---|---|---|
| id | UUID | PK |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| mot_de_passe | VARCHAR(255) | NOT NULL (hashé bcrypt) |
| role | ENUM | ADMIN, ORGANISATEUR, REFERENT_ORGANISATION |
| organisation_id | UUID | FK → ORGANISATION, nullable |
| actif | BOOLEAN | default true |
| created_at | TIMESTAMP | NOT NULL |
| derniere_connexion | TIMESTAMP | nullable |

> Le rôle `REFERENT_ORGANISATION` donne un accès limité : consultation des missions de son organisation et validation de ses membres uniquement.

#### ORGANISATION
Représente une entité externe intervenant sur un événement (association partenaire, entreprise prestataire).

| Champ | Type | Contrainte |
|---|---|---|
| id | UUID | PK |
| nom | VARCHAR(255) | NOT NULL |
| type | ENUM | ASSOCIATION, ENTREPRISE, AUTRE |
| siret | VARCHAR(14) | nullable (entreprises) |
| email_contact | VARCHAR(255) | NOT NULL |
| telephone_contact | VARCHAR(20) | nullable |
| adresse | TEXT | nullable |
| created_at | TIMESTAMP | NOT NULL |

#### EVENEMENT
Représente un festival ou événement géré dans l'application.

| Champ | Type | Contrainte |
|---|---|---|
| id | UUID | PK |
| nom | VARCHAR(255) | NOT NULL |
| description | TEXT | nullable |
| date_debut | DATE | NOT NULL |
| date_fin | DATE | NOT NULL |
| lieu | VARCHAR(255) | NOT NULL |
| statut | ENUM | BROUILLON, PUBLIE, ARCHIVE |
| organisateur_id | UUID | FK → UTILISATEUR |
| created_at | TIMESTAMP | NOT NULL |

#### MISSION
Représente un poste de travail sur un événement (ex : "Accueil entrée principale").

| Champ | Type | Contrainte |
|---|---|---|
| id | UUID | PK |
| evenement_id | UUID | FK → EVENEMENT |
| organisation_id | UUID | FK → ORGANISATION, nullable |
| nom | VARCHAR(255) | NOT NULL |
| description | TEXT | nullable |
| categorie | ENUM | ROADIE, ACCUEIL, SECURITE, CATERING, COMMUNICATION, LOGISTIQUE |
| nb_benevoles_requis | INT | NOT NULL |
| lieu | VARCHAR(255) | nullable (ex : Scène A, Entrée Nord) |
| materiel_requis | TEXT | nullable |
| multi_affectation_autorisee | BOOLEAN | default false |
| geree_par_organisation | BOOLEAN | default false |
| created_at | TIMESTAMP | NOT NULL |

> Si `geree_par_organisation = true` et `organisation_id` est renseigné, la mission est entièrement sous la responsabilité de l'organisation prestataire. L'organisateur principal garde la visibilité mais ne gère pas les affectations individuelles.

#### CRENEAU
Représente un créneau horaire d'une mission. Une mission peut avoir plusieurs créneaux.

| Champ | Type | Contrainte |
|---|---|---|
| id | UUID | PK |
| mission_id | UUID | FK → MISSION |
| debut | DATETIME | NOT NULL |
| fin | DATETIME | NOT NULL |
| nb_benevoles_requis | INT | NOT NULL |

#### BENEVOLE
Représente une personne bénévole. Contient toutes les données personnelles — point central RGPD.

| Champ | Type | Contrainte |
|---|---|---|
| id | UUID | PK |
| nom | VARCHAR(100) | NOT NULL |
| prenom | VARCHAR(100) | NOT NULL |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| telephone | VARCHAR(20) | nullable |
| competences | TEXT | nullable |
| taille_tshirt | ENUM | XS, S, M, L, XL, XXL, nullable |
| date_naissance | DATE | nullable |
| disponibilites | TEXT | nullable (jours/créneaux souhaités avant affectation) |
| organisation_id | UUID | FK → ORGANISATION, nullable |
| statut_compte | ENUM | INVITE, INSCRIT, VALIDE, ANONYMISE |
| consentement_rgpd | BOOLEAN | NOT NULL |
| date_consentement | TIMESTAMP | NOT NULL |
| version_cgu | VARCHAR(10) | NOT NULL |
| date_anonymisation | TIMESTAMP | nullable |
| created_at | TIMESTAMP | NOT NULL |

> Un bénévole peut être rattaché à une organisation (`organisation_id` renseigné) ou être indépendant (null). Quand `statut_compte = ANONYMISE`, les champs nom, prénom, email et téléphone sont remplacés par des valeurs neutres ; l'UUID et les affectations historiques sont conservés à des fins statistiques.

#### AFFECTATION
Table pivot entre un bénévole et un créneau de mission.

| Champ | Type | Contrainte |
|---|---|---|
| id | UUID | PK |
| benevole_id | UUID | FK → BENEVOLE |
| creneau_id | UUID | FK → CRENEAU |
| statut | ENUM | EN_ATTENTE, CONFIRME, REFUSE, ANNULE |
| commentaire | TEXT | nullable |
| created_at | TIMESTAMP | NOT NULL |

> **Règle de contrainte :** Lors d'une affectation, vérifier l'absence de chevauchement horaire pour ce bénévole sur le même événement (sauf si `multi_affectation_autorisee = true` sur la mission).

#### ACCREDITATION
Représente le badge d'accès d'un bénévole pour un événement.

| Champ | Type | Contrainte |
|---|---|---|
| id | UUID | PK |
| benevole_id | UUID | FK → BENEVOLE |
| evenement_id | UUID | FK → EVENEMENT |
| type | ENUM | BENEVOLE, STAFF, PRESSE, ARTISTE |
| code_qr | VARCHAR(255) | UNIQUE, généré automatiquement |
| zones_acces | ENUM | GENERAL, SCENE, BACKSTAGE, VIP (multi-valeurs possible) |
| date_debut_validite | DATE | nullable |
| date_fin_validite | DATE | nullable |
| valide | BOOLEAN | default false |
| date_emission | TIMESTAMP | nullable |

#### JOURNAL_AUDIT *(RGPD)*
Trace tous les accès et modifications sur les données personnelles.

| Champ | Type | Contrainte |
|---|---|---|
| id | UUID | PK |
| utilisateur_id | UUID | FK → UTILISATEUR |
| action | ENUM | LECTURE, CREATION, MODIFICATION, SUPPRESSION, EXPORT, ANONYMISATION |
| entite_cible | VARCHAR(50) | ex : "BENEVOLE" |
| entite_id | UUID | ID de la ressource accédée |
| ip_address | VARCHAR(45) | nullable |
| timestamp | TIMESTAMP | NOT NULL |
| detail | TEXT | nullable (ex : champs modifiés) |

### Schéma des relations

```
UTILISATEUR ──< EVENEMENT
UTILISATEUR >── ORGANISATION
ORGANISATION ──< BENEVOLE
ORGANISATION ──< MISSION
EVENEMENT   ──< MISSION
MISSION     ──< CRENEAU
CRENEAU     ──< AFFECTATION >── BENEVOLE
EVENEMENT   ──< ACCREDITATION >── BENEVOLE
UTILISATEUR ──< JOURNAL_AUDIT
```

---

## 4. Gestion des rôles et droits d'accès

| Rôle | Droits |
|---|---|
| ADMIN | Accès total, gestion des utilisateurs et organisations |
| ORGANISATEUR | Gestion complète d'un événement (missions, bénévoles, créneaux, accréditations) |
| REFERENT_ORGANISATION | Consultation de ses missions uniquement, validation de ses membres, aucun accès aux autres données |
| BENEVOLE (portail) | Consultation de ses propres affectations, export de ses données (RGPD), gestion de son consentement |

---

## 5. Conformité RGPD

### Base légale du traitement

Le traitement des données personnelles des bénévoles repose sur le **consentement explicite** (article 6.1.a du RGPD), recueilli lors de l'inscription et traçable en base.

### Droits des personnes

| Droit | Article RGPD | Implémentation technique |
|---|---|---|
| Droit d'accès | Art. 15 | Export JSON de toutes les données du bénévole via son espace personnel |
| Droit de rectification | Art. 16 | Modification de ses données depuis son profil |
| Droit à l'effacement | Art. 17 | Anonymisation des champs personnels, conservation des données statistiques agrégées |
| Droit à la portabilité | Art. 20 | Export JSON standardisé |
| Droit d'opposition | Art. 21 | Retrait du consentement, désactivation du compte |

### Mesures techniques

- Mots de passe hashés avec **bcrypt** (coût 12 minimum)
- Communications chiffrées en **HTTPS** (TLS 1.2 minimum)
- Données personnelles **jamais loguées** dans les logs applicatifs
- Accès aux données personnelles **tracé** dans JOURNAL_AUDIT
- Durée de conservation définie et automatisable (job de purge/anonymisation)
- Cloisonnement des données entre organisations (un référent ne voit que ses membres)
- Aucune transmission à des tiers sans consentement

### Mentions obligatoires

L'application doit afficher :
- Une **politique de confidentialité** accessible avant inscription
- Les **CGU versionnées** (la version acceptée est stockée par bénévole)
- Un **bandeau de consentement** clair à l'inscription (pas de cases pré-cochées)

---

## 6. Lancement du projet

### Prérequis

| Outil | Version minimale | Installation |
|---|---|---|
| Docker Desktop | 4.x | https://www.docker.com/products/docker-desktop |
| Docker Compose | V2 (intégré à Docker Desktop) | Inclus dans Docker Desktop |
| Git | 2.x | https://git-scm.com |
| Java 17 *(dev local uniquement)* | 17 LTS | https://adoptium.net |
| Node.js *(dev local uniquement)* | 20 LTS | https://nodejs.org |

> En production / démo, seuls **Docker Desktop** et **Git** sont nécessaires. Java et Node ne sont utilisés qu'en développement local.

---

### Lancement avec Docker *(recommandé)*

```bash
# 1. Cloner le dépôt
git clone https://github.com/<utilisateur>/FestManager.git
cd FestManager

# 2. Construire les images
docker compose build

# 3. Démarrer la stack complète (PostgreSQL + Backend + Frontend)
docker compose up

# 4. Arrêter la stack
docker compose down

# Arrêter et supprimer les volumes (repart de zéro en BDD)
docker compose down -v
```

Une fois démarré :

| Service | URL |
|---|---|
| Application (Frontend Angular) | http://localhost:4200 |
| API Backend (Spring Boot) | http://localhost:8080 |
| Base de données PostgreSQL | localhost:5432 |

> Le frontend proxifie automatiquement les appels `/api/*` et `/ws/*` vers le backend via nginx.  
> Aucune configuration CORS manuelle n'est nécessaire en mode Docker.

---

### Lancement en développement local *(sans Docker)*

Le profil `dev` utilise une base **H2 embarquée en mémoire** — aucune installation de PostgreSQL requise.

**Backend :**
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# API disponible sur http://localhost:8080
# Console H2 disponible sur http://localhost:8080/h2-console
```

**Frontend :**
```bash
cd frontend
npm install
npm start
# Application disponible sur http://localhost:4200
```

> En mode dev local, le proxy Angular (`proxy.conf.json`) redirige `/api/*` vers `http://localhost:8080`.

---

### Variables d'environnement Docker

Les variables sont définies directement dans `docker-compose.yml`. Pour personnaliser sans modifier le fichier, créer un fichier `.env` à la racine :

```env
POSTGRES_DB=festmanager
POSTGRES_USER=festmanager
POSTGRES_PASSWORD=mon_mot_de_passe_securise
```

---

## 7. Architecture applicative

### Vue d'ensemble

```
[ Angular Frontend ]
        |
   HTTP REST + WebSocket (STOMP)
        |
[ Spring Boot API ]
        |
   JPA / Hibernate
        |
[ PostgreSQL ]
```

### Structure du projet backend (Spring Boot)

```
festmanager-backend/
├── src/main/java/com/festmanager/
│   ├── config/          # Spring Security, WebSocket, CORS
│   ├── controller/      # REST Controllers
│   ├── service/         # Logique métier
│   ├── repository/      # Interfaces JPA
│   ├── entity/          # Entités JPA
│   ├── dto/             # Data Transfer Objects
│   ├── mapper/          # Entity <-> DTO
│   ├── audit/           # Journal d'audit RGPD
│   └── websocket/       # Handlers WebSocket
├── src/main/resources/
│   ├── application.yml
│   └── application-docker.yml
└── Dockerfile
```

### Structure du projet frontend (Angular)

```
festmanager-frontend/
├── src/app/
│   ├── core/            # Guards, Interceptors, Auth service
│   ├── shared/          # Composants réutilisables
│   ├── features/
│   │   ├── evenements/
│   │   ├── benevoles/
│   │   ├── organisations/
│   │   ├── missions/
│   │   ├── planning/
│   │   ├── accreditations/
│   │   └── dashboard/
│   └── app-routing.module.ts
└── Dockerfile
```

---

## 8. Fonctionnalités

### MVP (version 1.0 — objectif recruteur)

| # | Fonctionnalité | Priorité |
|---|---|---|
| F01 | Authentification JWT (login/logout) | Haute |
| F02 | Gestion des événements (CRUD) | Haute |
| F03 | Gestion des missions et créneaux | Haute |
| F04 | Inscription bénévoles (3 flux) | Haute |
| F05 | Affectation bénévoles aux créneaux avec contrôle conflits | Haute |
| F06 | Tableau de bord temps réel (WebSocket) | Haute |
| F07 | Gestion des organisations prestataires | Haute |
| F08 | Espace référent organisation (accès limité) | Haute |
| F09 | Recueil et traçabilité du consentement RGPD | Haute |
| F10 | Export données bénévole (droit d'accès RGPD) | Haute |
| F11 | Anonymisation bénévole (droit à l'effacement RGPD) | Haute |
| F12 | Journal d'audit des accès aux données personnelles | Haute |
| F13 | Génération d'accréditations (QR code) | Moyenne |
| F14 | Export planning (PDF ou CSV) | Moyenne |
| F15 | Politique de confidentialité et CGU versionnées | Moyenne |
| F16 | Notifications email (SMTP) | Basse |
| F17 | Page Mentions Légales (éditeur, hébergeur, contact DPO) | Haute |

### Hors périmètre MVP

- Application mobile native
- Paiement / billetterie
- Gestion comptable
- Multi-tenant SaaS

---

## 9. Règles de travail et conventions

### Git

- Branche principale : `main` (toujours stable et déployable)
- Branche de développement : `develop`
- Branches de fonctionnalités : `feature/nom-de-la-feature`
- Chaque feature est mergée via Pull Request avec description
- Commits en français, conventionnels : `feat:`, `fix:`, `docs:`, `refactor:`

### Validation

- **Aucune étape ne commence sans validation explicite de la précédente**
- Chaque tâche possède une définition de "terminé" (Definition of Done)
- Le modèle de données doit être validé avant de créer la moindre entité JPA

### Qualité

- Couverture de tests backend : objectif 70% minimum sur les services
- Aucun endpoint REST sans test d'intégration
- Code commenté en français

---

## 10. Roadmap

### Phase 0 — Cadrage ✅

| ID | Tâche | Statut |
|---|---|---|
| T00-01 | Définir le besoin et le contexte | ✅ Validé |
| T00-02 | Choisir la stack technique | ✅ Validé |
| T00-03 | Modéliser les entités et relations | ✅ Validé |
| T00-04 | Rédiger la documentation initiale | ✅ Validé |
| T00-05 | Créer le repo GitHub avec README | ✅ Validé |

### Phase 1 — Fondations techniques ✅

| ID | Tâche | Statut |
|---|---|---|
| T01-01 | Initialiser le projet Spring Boot | ✅ Validé |
| T01-02 | Initialiser le projet Angular | ✅ Validé |
| T01-03 | Mettre en place Docker Compose | ✅ Validé |
| T01-04 | Configurer Spring Security + JWT | ✅ Validé |
| T01-05 | Créer les entités JPA et migrations Flyway | ✅ Validé |
| T01-06 | Mettre en place GitHub Actions | ✅ Validé |

### Phase 2 — Core features ✅

| ID | Tâche | Statut |
|---|---|---|
| T02-01 | API REST : Événements (CRUD) | ✅ Validé |
| T02-02 | API REST : Organisations (CRUD + accès référent) | ✅ Validé |
| T02-03 | API REST : Missions et créneaux | ✅ Validé |
| T02-04 | API REST : Bénévoles (3 flux + RGPD) | ✅ Validé |
| T02-05 | API REST : Affectations + contrôle conflits | ✅ Validé |
| T02-06 | API REST : Journal d'audit (lecture admin) | ✅ Validé |
| T02-07 | WebSocket : canal dashboard temps réel | ✅ Validé |
| T02-08 | Frontend : Authentification (login + inscription) | ✅ Validé |
| T02-09 | Frontend : Événements | ✅ Validé |
| T02-10 | Frontend : Organisations et référents | ✅ Validé |
| T02-11 | Frontend : Bénévoles + espace RGPD | ✅ Validé |
| T02-12 | Frontend : Planning / affectations temps réel | ✅ Validé |
| T02-13 | Dockerisation complète (Dockerfiles + nginx.conf) | ✅ Validé |

### Phase 3 — Features avancées (en cours)

| ID | Tâche | Statut |
|---|---|---|
| T03-01 | Génération QR code accréditations | ✅ Validé |
| T03-02 | Dashboard temps réel complet | ✅ Validé |
| T03-03 | Export planning CSV/PDF | ✅ Validé |
| T03-04 | Job automatique d'anonymisation (purge RGPD) | ✅ Validé |
| T03-05 | Notifications email (SMTP) | ✅ Validé |
| T03-06 | Page Mentions Légales | ✅ Validé |

### Phase 4 — Finition recruteur

| ID | Tâche | Statut |
|---|---|---|
| T04-01 | Tests unitaires et d'intégration complets | 🔲 À faire |
| T04-02 | Documentation API (Swagger / OpenAPI) | ✅ Validé |
| T04-03 | Déploiement démo en ligne (Railway) | ✅ Validé |
| T04-04 | README final avec screenshots et lien démo | ✅ Validé |
| T04-05 | Diagramme d'architecture dans le README | ✅ Validé |

---

## 11. Déploiement Railway

### Prérequis
- Compte Railway (railway.app) connecté au dépôt GitHub
- CLI Railway installé : `npm install -g @railway/cli`

### Services à créer dans Railway

| Service | Source | Répertoire build |
|---|---|---|
| `backend` | GitHub (ce dépôt) | `backend/` |
| `frontend` | GitHub (ce dépôt) | `frontend/` |
| `postgres` | Plugin Railway | — |

### Variables d'environnement — service `backend`

| Variable | Valeur |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `docker` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
| `SPRING_DATASOURCE_USERNAME` | `${{Postgres.PGUSER}}` |
| `SPRING_DATASOURCE_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
| `APP_JWT_SECRET` | Générer avec `openssl rand -base64 64` |
| `APP_JWT_EXPIRATION_MS` | `86400000` |
| `ALLOWED_ORIGINS` | URL du service frontend Railway (ex: `https://festmanager-frontend.up.railway.app`) |

### Variables d'environnement — service `frontend`

| Variable | Valeur |
|---|---|
| `BACKEND_URL` | URL interne Railway du backend (ex: `http://backend.railway.internal:8080`) |
| `NGINX_ENVSUBST_TEMPLATE_VARS` | `BACKEND_URL` |

### Build settings

- Backend : Railway détecte `backend/Dockerfile` automatiquement. Root directory = `backend`.
- Frontend : Railway détecte `frontend/Dockerfile` automatiquement. Root directory = `frontend`.

### Notes importantes

- `PORT` est injecté automatiquement par Railway dans le backend Spring Boot.
- Le JWT secret doit être une chaîne longue et aléatoire en production (minimum 32 caractères).
- Les migrations Flyway s'exécutent automatiquement au premier démarrage.
- L'URL Swagger sera disponible sur `https://<backend-url>/swagger-ui.html`.

## 12. Prochaine étape immédiate

> **Phase 4 complète. Projet portfolio terminé.**
