# FestManager — Documentation du projet

> Version : 0.2  
> Auteur : Alan  
> Date : Avril 2026  
> Statut : **En cours de définition — modèle de données à valider**

---

## Historique des versions

| Version | Date | Modifications |
|---|---|---|
| 0.1 | Avril 2026 | Document initial |
| 0.2 | Avril 2026 | Ajout organisations prestataires + conformité RGPD |

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

## 6. Architecture applicative

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

## 7. Fonctionnalités

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

### Hors périmètre MVP

- Application mobile native
- Paiement / billetterie
- Gestion comptable
- Multi-tenant SaaS

---

## 8. Règles de travail et conventions

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

## 9. Roadmap

### Phase 0 — Cadrage (en cours)

| ID | Tâche | Statut |
|---|---|---|
| T00-01 | Définir le besoin et le contexte | ✅ Validé |
| T00-02 | Choisir la stack technique | ✅ Validé |
| T00-03 | Modéliser les entités et relations | 🔲 **À valider** |
| T00-04 | Rédiger la documentation initiale | 🔲 En cours |
| T00-05 | Créer le repo GitHub avec README | 🔲 À faire |

### Phase 1 — Fondations techniques (Semaine 1-2)

| ID | Tâche | Dépend de |
|---|---|---|
| T01-01 | Initialiser le projet Spring Boot | T00-05 |
| T01-02 | Initialiser le projet Angular | T00-05 |
| T01-03 | Mettre en place Docker Compose | T01-01, T01-02 |
| T01-04 | Configurer Spring Security + JWT | T01-01 |
| T01-05 | Créer les entités JPA et migrations Flyway | T00-03 validé |
| T01-06 | Mettre en place GitHub Actions | T01-01, T01-02 |

### Phase 2 — Core features (Semaine 3-6)

| ID | Tâche | Dépend de |
|---|---|---|
| T02-01 | API REST : Événements (CRUD) | Phase 1 |
| T02-02 | API REST : Organisations (CRUD + accès référent) | Phase 1 |
| T02-03 | API REST : Missions et créneaux | T02-01 |
| T02-04 | API REST : Bénévoles (3 flux + RGPD) | Phase 1 |
| T02-05 | API REST : Affectations + contrôle conflits | T02-03, T02-04 |
| T02-06 | API REST : Journal d'audit (lecture admin) | Phase 1 |
| T02-07 | WebSocket : canal dashboard temps réel | T02-05 |
| T02-08 | Frontend : Authentification | Phase 1 |
| T02-09 | Frontend : Événements | T02-01 |
| T02-10 | Frontend : Organisations et référents | T02-02 |
| T02-11 | Frontend : Bénévoles + espace RGPD | T02-04 |
| T02-12 | Frontend : Planning / affectations temps réel | T02-05, T02-07 |

### Phase 3 — Features avancées (Semaine 7-10)

| ID | Tâche | Dépend de |
|---|---|---|
| T03-01 | Génération QR code accréditations | Phase 2 |
| T03-02 | Dashboard temps réel complet | T02-07 |
| T03-03 | Export planning CSV/PDF | Phase 2 |
| T03-04 | Job automatique d'anonymisation (purge RGPD) | Phase 2 |
| T03-05 | Notifications email (SMTP) | Phase 2 |

### Phase 4 — Finition recruteur (Semaine 11-12)

| ID | Tâche | Dépend de |
|---|---|---|
| T04-01 | Tests unitaires et d'intégration complets | Phase 3 |
| T04-02 | Documentation API (Swagger / OpenAPI) | Phase 3 |
| T04-03 | Déploiement démo en ligne (Railway) | Phase 3 |
| T04-04 | README final avec screenshots et lien démo | T04-03 |
| T04-05 | Diagramme d'architecture dans le README | T04-04 |

---

## 10. Prochaine étape immédiate

> **Tâche active : T00-03 — Validation du modèle de données**

Points à confirmer avant tout démarrage du développement :

1. Les 8 entités (dont ORGANISATION et JOURNAL_AUDIT) couvrent-elles tous les besoins terrain ?
2. La distinction `geree_par_organisation` (mission entièrement déléguée) vs bénévoles rattachés à une organisation te semble-t-elle juste ?
3. Le niveau d'accès du référent organisation (voir ses missions + valider ses membres uniquement) correspond-il à ton besoin réel ?
4. Les mesures RGPD listées sont-elles suffisantes ou manque-t-il quelque chose ?
5. Des champs manquent-ils sur certaines entités ?

**Validation = go pour T00-05 (création du repo GitHub) et T01-05 (entités JPA).**
