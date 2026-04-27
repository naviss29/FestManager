# FestManager — Documentation du projet

> Version : 1.0  
> Auteur : Alan  
> Date : Avril 2026  
> Statut : **Phase 6 en cours — pipeline de recette**

---

## Historique des versions

| Version | Date | Modifications |
|---|---|---|
| 0.1 | Avril 2026 | Document initial |
| 0.2 | Avril 2026 | Ajout organisations prestataires + conformité RGPD |
| 0.3 | Avril 2026 | Phases 1 et 2 complétées — backend + frontend fonctionnels |
| 0.4 | Avril 2026 | Dockerisation complète — ajout section lancement et prérequis |
| 0.5 | Avril 2026 | Phase 3 complète — QR codes, dashboard, exports, RGPD, email, mentions légales |
| 0.6 | Avril 2026 | Phase 5 — Portail inscription public, validation admin, stockage fichiers (photos/bannières) |
| 0.7 | Avril 2026 | Phase 5 terminée — Reset mot de passe, créneaux horaires CRUD, élimination N+1 dashboard/affectations |
| 0.8 | Avril 2026 | Phase 6 — Pipeline de recette : branche develop, deploy staging automatique, approbation manuelle prod |
| 0.9 | Avril 2026 | Fix change detection Angular 21 — ChangeDetectorRef.detectChanges() sur tous les composants |
| 1.0 | Avril 2026 | F23 Connexion Google OAuth (staff) — F24 Portail self-service bénévole (profil + photo badge, magic link) |
| 1.1 | Avril 2026 | CI vert : fix mocks AffectationServiceTest (N+1), env.prod frontend, tests Vitest (JWT réel, websocket contrat public) |

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
| CI/CD | GitHub Actions | CI (tests) + pipeline de recette (staging auto / prod sur approbation) |
| Hébergement démo | Hetzner VPS CX22 + Coolify | Auto-hébergé, ~4€/mois, staging + prod |
| Tests backend | JUnit 5 + Mockito | Standard Spring Boot |
| Tests frontend | Vitest (Angular `@angular/build:unit-test`) | Runner intégré Angular 21 |
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
| reset_token | VARCHAR(255) | nullable (token de réinitialisation du mot de passe) |
| reset_token_expiry | TIMESTAMP | nullable (expiration 1h après génération) |
| google_id | VARCHAR(255) | UNIQUE, nullable (sub Google Identity Services — lié si connexion OAuth) |

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
| banniere_url | VARCHAR(512) | nullable (chemin relatif dans uploads/evenements/) |
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
| categorie | VARCHAR(100) | Libre (suggestions : ROADIE, ACCUEIL, SECURITE, CATERING, COMMUNICATION, LOGISTIQUE) |
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
| photo_url | VARCHAR(512) | nullable (chemin relatif dans uploads/benevoles/) |
| consentement_rgpd | BOOLEAN | NOT NULL |
| date_consentement | TIMESTAMP | NOT NULL |
| version_cgu | VARCHAR(10) | NOT NULL |
| profil_token | VARCHAR(255) | UNIQUE, nullable (token magic link accès portail self-service, 24h) |
| profil_token_expiry | TIMESTAMP | nullable (expiration du profil_token) |
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

## 7. Performances — Optimisations N+1

### Problème N+1 expliqué

Le problème N+1 survient quand on charge une liste de N entités puis qu'on accède à une association lazy (ex: `mission.getCreneaux()`) pour chacune : Hibernate exécute 1 requête pour la liste + N requêtes pour les associations = N+1 requêtes. Sur 20 missions, cela représente 21 requêtes là où une seule suffit.

### Corrections appliquées

| Fichier | Problème corrigé | Solution |
|---------|-----------------|----------|
| `AffectationRepository` | Export CSV/PDF : native query sans fetch sur `benevole` et `mission` | JPQL avec `JOIN FETCH` benevole + creneau + mission |
| `MissionRepository` | `findByEvenementId(UUID)` sans `@EntityGraph` — `getCreneaux()` déclenchait N requêtes | `@EntityGraph({"creneaux", ...})` sur la version non paginée |
| `DashboardService` | `findByEvenementId` appelé 2× + `getCreneaux()` lazy dans la boucle | Chargement unique réutilisé ; creneaux pré-chargés via EntityGraph |
| `CreneauMapper` | 1 `COUNT` SQL par créneau dans `listerCreneaux` | Surcharge `toResponse(creneau, int)` + batch `GROUP BY` en 1 requête dans `CreneauService` |
| `JournalAuditRepository` | `utilisateur` LAZY non chargé lors du mapping | `@EntityGraph({"utilisateur"})` sur les deux méthodes |
| `DashboardService.missionStat()` | 1 `COUNT` SQL par mission au snapshot (N missions = N requêtes) | `countParMissionsGrouped()` batch `GROUP BY` en 1 requête, résultat passé en paramètre |
| `CreneauRepository` | `findById()` sans EntityGraph — lazy loads chaînés sur `mission` + `evenement` dans `AffectationService.affecter()` | `findByIdWithMissionAndEvenement()` avec `@EntityGraph({"mission","mission.evenement"})` |
| `AffectationRepository` | `findById()` sans EntityGraph — lazy loads dans le mapper et `notifierAffectation()` WebSocket | `findByIdWithAssociations()` avec `@EntityGraph` complet (benevole, creneau, mission, evenement) |

### Règles pour les futurs développements

1. **Toujours mettre `@EntityGraph`** sur les méthodes de repository retournant des listes, quand les associations sont accédées dans le mapper.
2. **Ne jamais appeler un repository dans un mapper pour une liste** — calculer les données en batch dans le service et les passer en paramètre.
3. **Réutiliser une liste déjà chargée** plutôt que rappeler le même `findBy...` deux fois dans la même méthode.
4. **Préférer JPQL avec `JOIN FETCH`** aux requêtes natives quand on a besoin de pre-fetch (`@EntityGraph` ne fonctionne pas avec `nativeQuery = true`).

---

## 8. Architecture applicative

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
│   │   ├── dashboard/
│   │   └── mon-profil/          # Portail self-service bénévole
│   │       ├── demander-lien/   # Formulaire demande de lien (anti-énumération)
│   │       ├── connexion-benevole/ # Validation token → session localStorage
│   │       ├── profil-benevole/ # Édition profil + upload photo (protégé par BenevoleGuard)
│   │       ├── guards/          # BenevoleGuard (canActivate)
│   │       ├── services/        # BenevoleProfilService, BenevoleSessionService
│   │       └── models/          # BenevoleProfilResponse, BenevoleProfilUpdateRequest
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
| F18 | Récupération de mot de passe par email (token 1h, anti-énumération) | Haute |
| F19 | Gestion des créneaux horaires CRUD depuis la liste des missions | Haute |
| F20 | Stockage de fichiers (photos bénévoles, bannières événements, sécurité path traversal) | Moyenne |
| F21 | Portail d'inscription public (`/inscription`, sans authentification) | Haute |
| F22 | Validation admin des nouveaux comptes (bootstrap 1er compte, file d'attente suivants) | Haute |
| F23 | Connexion Google OAuth pour le staff (Google Identity Services — login + inscription) | Haute |
| F24 | Portail self-service bénévole : magic link email → session localStorage → édition profil + upload photo badge | Haute |

### Hors périmètre MVP

- Application mobile native
- Paiement / billetterie
- Gestion comptable
- Multi-tenant SaaS

---

## 9. Règles de travail et conventions

### Git

- Branche principale : `main` (toujours stable, déployable — protégée)
- Branche de développement : `develop` → déploiement automatique en staging après CI vert
- Branches de fonctionnalités : `feature/nom-de-la-feature` → mergées dans `develop` via PR
- `develop` → `main` uniquement via PR + CI vert + approbation manuelle → déclenche le déploiement production
- Commits en français, conventionnels : `feat:`, `fix:`, `docs:`, `refactor:`

### Validation

- **Aucune étape ne commence sans validation explicite de la précédente**
- Chaque tâche possède une définition de "terminé" (Definition of Done)
- Le modèle de données doit être validé avant de créer la moindre entité JPA

### Qualité

- Couverture de tests backend : **95 tests Mockito** couvrant tous les services (Auth, Bénévoles, Événements, Missions, Organisations, Créneaux, Affectations, Journal d'audit, Audit, Export, Badges)
- Couverture de tests frontend : **91 tests Vitest** couvrant tous les services HTTP et composants clés
- Aucun endpoint REST sans test unitaire service
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

### Phase 3 — Features avancées ✅

| ID | Tâche | Statut |
|---|---|---|
| T03-01 | Génération QR code accréditations | ✅ Validé |
| T03-02 | Dashboard temps réel complet | ✅ Validé |
| T03-03 | Export planning CSV/PDF | ✅ Validé |
| T03-04 | Job automatique d'anonymisation (purge RGPD) | ✅ Validé |
| T03-05 | Notifications email (SMTP) | ✅ Validé |
| T03-06 | Page Mentions Légales | ✅ Validé |
| T03-07 | Génération de badges PDF imprimables (A6, QR code, ZIP multi-badges) | ✅ Validé |

### Phase 4 — Finition recruteur

| ID | Tâche | Statut |
|---|---|---|
| T04-01 | Tests unitaires et d'intégration complets | ✅ Validé |
| T04-02 | Documentation API (Swagger / OpenAPI) | ✅ Validé |
| T04-03 | Déploiement démo en ligne (Railway) | ✅ Validé |
| T04-04 | README final avec screenshots et lien démo | ✅ Validé |
| T04-05 | Diagramme d'architecture dans le README | ✅ Validé |

---

## 11. Déploiement — Hetzner VPS + Coolify

### Architecture d'hébergement

```
GitHub Actions (CI + tests)
        │ webhook POST
        ▼
  Coolify (PaaS auto-hébergé)
        │
  Hetzner CX22 (Ubuntu 24.04, ~4€/mois)
  ├── Environment staging   ← branche develop (auto après CI vert)
  │   ├── PostgreSQL staging
  │   ├── Backend Spring Boot staging
  │   └── Frontend Angular staging
  └── Environment production ← branche main (approbation manuelle requise)
      ├── PostgreSQL production
      ├── Backend Spring Boot production
      └── Frontend Angular production
```

### Prérequis

- Compte [hetzner.com](https://hetzner.com) avec un VPS CX22 (Ubuntu 24.04)
- Coolify installé sur le VPS (`curl -fsSL https://cdn.coollabs.io/coolify/install.sh | bash`)
- Accès Coolify sur `http://<IP_VPS>:8000` puis domaine configuré

### Services à créer dans Coolify

Pour chaque environment (`staging` et `production`), créer 3 services :

| Service | Type | Source |
|---|---|---|
| `postgres` | Plugin PostgreSQL Coolify | — |
| `backend` | Docker (Dockerfile) | GitHub, répertoire `backend/` |
| `frontend` | Docker (Dockerfile) | GitHub, répertoire `frontend/` |

### Variables d'environnement — service `backend`

| Variable | Valeur |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `docker` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<host_postgres_interne>:5432/festmanager` |
| `SPRING_DATASOURCE_USERNAME` | utilisateur PostgreSQL Coolify |
| `SPRING_DATASOURCE_PASSWORD` | mot de passe PostgreSQL Coolify |
| `APP_JWT_SECRET` | Générer avec `openssl rand -base64 64` |
| `APP_JWT_EXPIRATION_MS` | `86400000` |
| `ALLOWED_ORIGINS` | URL du frontend (ex: `https://festmanager.mondomaine.fr`) |
| `GOOGLE_CLIENT_ID` | Client ID Google OAuth 2.0 — laisser vide pour désactiver |

### Variables d'environnement — service `frontend`

| Variable | Valeur |
|---|---|
| `BACKEND_URL` | URL interne Coolify du backend (réseau Docker interne) |
| `NGINX_ENVSUBST_TEMPLATE_VARS` | `BACKEND_URL` |

### Secrets GitHub requis

Dans Settings → Secrets and variables → Actions du repo :

| Secret | Description |
|---|---|
| `COOLIFY_STAGING_BACKEND_WEBHOOK` | URL webhook deploy backend staging (Coolify → Service → Webhooks) |
| `COOLIFY_STAGING_FRONTEND_WEBHOOK` | URL webhook deploy frontend staging |
| `COOLIFY_PROD_BACKEND_WEBHOOK` | URL webhook deploy backend production |
| `COOLIFY_PROD_FRONTEND_WEBHOOK` | URL webhook deploy frontend production |

### GitHub Environments requis

- **`staging`** : aucune protection — déploiement automatique sur CI vert de `develop`
- **`production`** : approbation manuelle requise — protège la prod pendant les présentations

### Notes importantes

- Les migrations Flyway s'exécutent automatiquement au premier démarrage du backend.
- Le JWT secret doit faire minimum 32 caractères en production.
- Coolify gère SSL/TLS automatiquement via Let's Encrypt si un domaine est configuré.
- Le réseau Docker interne Coolify permet la communication backend ↔ PostgreSQL sans exposer le port.

### Pièges connus

#### ⚠️ Angular 21 — change detection hors zone après appel HTTP

**Symptôme :** le loader reste affiché, les données ne s'affichent pas, les filtres retournent les résultats de la requête précédente — jusqu'à ce que l'utilisateur clique n'importe où dans la page.

**Cause :** Angular 21 (avec `NgModule` non-standalone) exécute les callbacks HTTP en dehors de la zone Angular. Change detection n'est donc pas déclenchée automatiquement après une réponse HTTP.

**Fix :** injecter `ChangeDetectorRef` dans chaque composant et appeler `this.cdr.detectChanges()` à la fin de chaque callback `next:` et `error:` dans les `subscribe()`.

```typescript
// Dans le constructeur
constructor(private cdr: ChangeDetectorRef) {}

// Dans chaque subscribe
this.service.lister(...).subscribe({
  next: data => {
    this.donnees = data;
    this.chargement = false;
    this.cdr.detectChanges(); // obligatoire
  },
  error: () => { this.chargement = false; this.cdr.detectChanges(); }
});
```

> Ce pattern est appliqué sur **tous les composants** du projet (listes, formulaires, dashboard). Ne pas l'omettre sur tout nouveau composant qui fait un appel HTTP et met à jour la vue.

#### ⚠️ Angular 21 — callbacks de bibliothèques externes (ex : Google Identity Services)

**Symptôme :** le spinner Google ne s'affiche pas, la navigation vers `/dashboard` ne se déclenche pas, ou les erreurs ne s'affichent pas après une connexion Google.

**Cause :** les callbacks enregistrés auprès de bibliothèques JS externes (Google Identity Services, Stripe, etc.) s'exécutent complètement hors de la zone Angular. `detectChanges()` seul ne suffit pas — Angular ne reprend même pas la main sur le contexte de la fonction.

**Fix :** envelopper le callback dans `NgZone.run()` et appeler `cdr.detectChanges()` à l'intérieur :

```typescript
constructor(private ngZone: NgZone, private cdr: ChangeDetectorRef) {}

ngAfterViewInit(): void {
  google.accounts.id.initialize({
    client_id: environment.googleClientId,
    callback: (response: any) =>
      this.ngZone.run(() => this.handleGoogleResponse(response))
  });
}

private handleGoogleResponse(response: any): void {
  this.chargementGoogle = true;
  this.cdr.detectChanges();
  // ... appel HTTP, puis navigate ou erreur + cdr.detectChanges()
}
```

> Règle : tout callback passé à une bibliothèque externe = `NgZone.run()` + `cdr.detectChanges()` à l'intérieur.

> **Pour générer le JWT secret sans openssl (Windows) :**
> ```powershell
> [Convert]::ToBase64String((1..64 | ForEach-Object { [byte](Get-Random -Max 256) }))
> ```

#### ⚠️ `BACKEND_URL` doit pointer vers le réseau Docker interne Coolify

**Symptôme :** toutes les requêtes `/api/*` retournent `502 Bad Gateway` ou `503 Service Unavailable` alors que le backend est démarré.

**Cause :** si `BACKEND_URL` pointe vers l'URL publique du backend, nginx proxifie vers le domaine public qui passe par le load balancer, créant une boucle ou un timeout.

**Fix :** utiliser le nom de service Docker interne Coolify (visible dans l'onglet **Network** du service backend) :

```
BACKEND_URL=http://<nom_service_docker_backend>:8080
```

Cette URL ne transite pas par l'extérieur — la communication reste dans le réseau Docker interne du VPS.

### Phase 5 — Gestion des comptes et améliorations ✅

| ID | Tâche | Statut |
|---|---|---|
| T05-01 | Portail d'inscription public (`/inscription`, sans auth) | ✅ Validé |
| T05-02 | Validation admin des nouveaux comptes | ✅ Validé |
| T05-03 | Stockage de fichiers (photos bénévoles, bannières événements) | ✅ Validé |
| T05-04 | Récupération de mot de passe par email (token 1h) | ✅ Validé |
| T05-05 | Gestion des créneaux horaires CRUD depuis la liste des missions | ✅ Validé |
| T05-06 | Élimination N+1 : dashboard (batch GROUP BY), affectations (EntityGraph chaîné) | ✅ Validé |
| T05-07 | Fix filtre Angular (ngModelChange) sur toutes les pages de liste | ✅ Validé |

### Phase 6 — Pipeline de recette

| ID | Tâche | Statut |
|---|---|---|
| T06-01 | Créer la branche `develop` | ✅ Validé |
| T06-02 | Workflow `deploy.yml` : staging auto sur CI vert de `develop` | ✅ Validé |
| T06-03 | Workflow `deploy.yml` : production sur approbation manuelle (GitHub Environment) | ✅ Validé |
| T06-04 | Provisionner VPS Hetzner CX22 + installer Coolify | ✅ Validé |
| T06-05 | Configurer GitHub secrets (4 webhooks Coolify) + GitHub Environments | ✅ Validé |
| T06-06 | Activer la protection de branche `main` (CI requis + approbation PR) | ⏳ En attente |
| T06-07 | Configurer `GOOGLE_CLIENT_ID` — backend Coolify (staging + prod) + `environment.prod.ts` frontend | 🔄 Partiel (`environment.prod.ts` ✅ — Coolify ⏳) |

### Phase 7 — OAuth + Portail bénévole self-service ✅

| ID | Tâche | Statut |
|---|---|---|
| T07-01 | Migration V5 Flyway : `google_id` sur UTILISATEUR, `profil_token` + `profil_token_expiry` sur BENEVOLE | ✅ Validé |
| T07-02 | Backend : endpoint `POST /api/auth/google` (validation tokeninfo Google, 3 cas : lien/connexion/création) | ✅ Validé |
| T07-03 | Backend : endpoints profil bénévole publics (`/api/benevoles/profil/**`) — demande lien, GET, PUT, upload photo | ✅ Validé |
| T07-04 | Backend : `EmailService.envoyerLienProfil()` — envoi du magic link `/mon-profil/connexion/{token}` | ✅ Validé |
| T07-05 | Frontend : bouton Google sur login + register (GIS renderButton, NgZone, placeholder si non configuré) | ✅ Validé |
| T07-06 | Frontend : module `mon-profil` — DemanderLien → ConnexionBenevole (validation token) → ProfilBenevole (édition + photo) | ✅ Validé |
| T07-07 | Frontend : `BenevoleSessionService` (localStorage `fm_bvl_token`) + `BenevoleGuard` | ✅ Validé |
| T07-08 | Frontend : upload photo bénévole (FileReader preview immédiat + multipart vers backend) | ✅ Validé |
| T07-09 | Configuration Coolify : ajouter variable `GOOGLE_CLIENT_ID` sur le service backend (staging + prod) | ⏳ En attente |

---

## 12. Pipeline de recette

### Architecture

```
feature/* ──► develop ──[CI vert]──► staging Coolify (auto)
                                         │
                                    validation manuelle
                                         │
develop ──► PR ──► main ──[CI vert + approbation GitHub]──► production Coolify
```

### Composants

| Composant | Rôle |
|---|---|
| `.github/workflows/ci.yml` | Build + tests backend (Maven) et frontend (Angular) sur push/PR |
| `.github/workflows/deploy.yml` | Déploiement via webhooks Coolify, déclenché après CI vert |
| GitHub Environment `staging` | Aucune protection — déploiement automatique sur `develop` |
| GitHub Environment `production` | Approbation manuelle requise — protège la prod |
| Coolify environment `staging` | Services Docker liés à `develop` |
| Coolify environment `production` | Services Docker liés à `main` |

### Secrets GitHub requis

| Secret | Description |
|---|---|
| `COOLIFY_STAGING_BACKEND_WEBHOOK` | URL webhook deploy backend staging (Coolify → Service → Webhooks) |
| `COOLIFY_STAGING_FRONTEND_WEBHOOK` | URL webhook deploy frontend staging |
| `COOLIFY_PROD_BACKEND_WEBHOOK` | URL webhook deploy backend production |
| `COOLIFY_PROD_FRONTEND_WEBHOOK` | URL webhook deploy frontend production |
| `COOLIFY_API_TOKEN` | Token API Coolify — **Keys & Tokens → API Tokens → permission `deploy`** — envoyé en `Authorization: Bearer` sur chaque webhook |

> **Pourquoi `COOLIFY_API_TOKEN` ?** Les webhooks Coolify nécessitent un header `Authorization: Bearer <token>` en plus de l'URL. Sans lui, Coolify retourne 401 même si l'URL est correcte. Le token se crée dans Coolify → Keys & Tokens → API Tokens → cocher `deploy` → durée 1 an.

### Configuration GOOGLE_CLIENT_ID

Le `GOOGLE_CLIENT_ID` est une valeur **publique** (visible dans le JS compilé) — ce n'est pas un secret. Il doit être configuré à **deux endroits** :

#### 1. Backend — variable Coolify (staging + production)

Dans chaque service backend Coolify → onglet "Environment Variables" :

```
GOOGLE_CLIENT_ID = xxxxxxxxxxxxxxx.apps.googleusercontent.com
```

Répéter pour le service staging ET le service production. Sans cette variable, le backend rejette les tokens Google avec une erreur 400.

#### 2. Frontend — `environment.prod.ts` (compilé dans le bundle)

Modifier `frontend/src/environments/environment.prod.ts` :

```typescript
export const environment = {
  production: true,
  apiUrl: '/api',
  wsUrl: '/ws',
  googleClientId: 'xxxxxxxxxxxxxxx.apps.googleusercontent.com'
};
```

> **Important :** Cette valeur est compilée dans le bundle Angular. Elle n'est **pas** un GitHub Secret — elle est visible dans les devtools du navigateur en production. C'est le comportement attendu pour un Client ID OAuth 2.0 (la sécurité repose sur les origines autorisées côté Google Cloud, pas sur le secret du Client ID).

Le Client ID se trouve dans : Google Cloud Console → APIs & Services → Credentials → OAuth 2.0 Client IDs.

#### Distinction protection d'environnement vs protection de branche

| Mécanisme | Où configurer | Effet |
|---|---|---|
| **GitHub Environment protection** (`production`) | Settings → Environments → production | Bloque le **job de déploiement** jusqu'à approbation manuelle — déjà configuré ✅ |
| **GitHub Branch protection** (`main`) | Settings → Branches → Add rule → `main` | Empêche les `git push --force` et les merges sans CI vert — à configurer ⏳ |

Pour activer la protection de branche `main` :
1. Settings → Branches → "Add branch protection rule"
2. Branch name pattern : `main`
3. Cocher : "Require status checks to pass before merging"
4. Ajouter le check CI : `backend-tests` et `frontend-tests` (noms des jobs dans `ci.yml`)
5. Cocher : "Require branches to be up to date before merging"
6. Optionnel : "Require a pull request before merging"

---

## 13. Prochaine étape immédiate

> L'infrastructure Coolify + CI/CD est en place. Le CI est maintenant vert (111 tests backend + 91 tests frontend). La protection d'environnement `production` (approbation manuelle de déploiement) est active.
>
> 1. Ajouter `GOOGLE_CLIENT_ID` dans les variables du service backend Coolify (staging + prod) — voir section "Configuration GOOGLE_CLIENT_ID" ci-dessus
> 2. Mettre à jour `environment.prod.ts` avec le vrai Client ID Google
> 3. Activer la protection de branche `main` sur GitHub (Settings → Branches) — CI requis avant merge
> 4. Respecter la discipline de branches : **développer sur `develop`**, merge vers `main` uniquement via PR validée par CI
