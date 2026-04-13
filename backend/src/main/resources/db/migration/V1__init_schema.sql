-- ============================================================
-- FestManager — Migration initiale du schéma de base de données
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ------------------------------------------------------------
-- ORGANISATION
-- ------------------------------------------------------------
CREATE TABLE organisation (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nom           VARCHAR(255) NOT NULL,
    type          VARCHAR(50)  NOT NULL CHECK (type IN ('ASSOCIATION', 'ENTREPRISE', 'AUTRE')),
    siret         VARCHAR(14),
    email_contact VARCHAR(255) NOT NULL,
    telephone_contact VARCHAR(20),
    adresse       TEXT,
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- UTILISATEUR
-- ------------------------------------------------------------
CREATE TABLE utilisateur (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email               VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe        VARCHAR(255) NOT NULL,
    role                VARCHAR(50)  NOT NULL CHECK (role IN ('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION')),
    organisation_id     UUID         REFERENCES organisation(id) ON DELETE SET NULL,
    actif               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    derniere_connexion  TIMESTAMP
);

-- ------------------------------------------------------------
-- EVENEMENT
-- ------------------------------------------------------------
CREATE TABLE evenement (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nom             VARCHAR(255) NOT NULL,
    description     TEXT,
    date_debut      DATE         NOT NULL,
    date_fin        DATE         NOT NULL,
    lieu            VARCHAR(255) NOT NULL,
    statut          VARCHAR(50)  NOT NULL DEFAULT 'BROUILLON' CHECK (statut IN ('BROUILLON', 'PUBLIE', 'ARCHIVE')),
    organisateur_id UUID         NOT NULL REFERENCES utilisateur(id),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- MISSION
-- ------------------------------------------------------------
CREATE TABLE mission (
    id                         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    evenement_id               UUID         NOT NULL REFERENCES evenement(id) ON DELETE CASCADE,
    organisation_id            UUID         REFERENCES organisation(id) ON DELETE SET NULL,
    nom                        VARCHAR(255) NOT NULL,
    description                TEXT,
    lieu                       VARCHAR(255),
    materiel_requis            TEXT,
    categorie                  VARCHAR(50)  NOT NULL CHECK (categorie IN ('ROADIE', 'ACCUEIL', 'SECURITE', 'CATERING', 'COMMUNICATION', 'LOGISTIQUE')),
    nb_benevoles_requis        INT          NOT NULL,
    multi_affectation_autorisee BOOLEAN     NOT NULL DEFAULT FALSE,
    geree_par_organisation     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at                 TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- CRENEAU
-- ------------------------------------------------------------
CREATE TABLE creneau (
    id                  UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    mission_id          UUID      NOT NULL REFERENCES mission(id) ON DELETE CASCADE,
    debut               TIMESTAMP NOT NULL,
    fin                 TIMESTAMP NOT NULL,
    nb_benevoles_requis INT       NOT NULL,
    CONSTRAINT creneau_debut_avant_fin CHECK (debut < fin)
);

-- ------------------------------------------------------------
-- BENEVOLE
-- ------------------------------------------------------------
CREATE TABLE benevole (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nom                 VARCHAR(100) NOT NULL,
    prenom              VARCHAR(100) NOT NULL,
    email               VARCHAR(255) NOT NULL UNIQUE,
    telephone           VARCHAR(20),
    competences         TEXT,
    taille_tshirt       VARCHAR(10)  CHECK (taille_tshirt IN ('XS', 'S', 'M', 'L', 'XL', 'XXL')),
    date_naissance      DATE,
    disponibilites      TEXT,
    statut_compte       VARCHAR(20)  NOT NULL DEFAULT 'INVITE' CHECK (statut_compte IN ('INVITE', 'INSCRIT', 'VALIDE', 'ANONYMISE')),
    consentement_rgpd   BOOLEAN      NOT NULL,
    date_consentement   TIMESTAMP    NOT NULL,
    version_cgu         VARCHAR(10)  NOT NULL,
    date_anonymisation  TIMESTAMP,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- AFFECTATION
-- ------------------------------------------------------------
CREATE TABLE affectation (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    benevole_id  UUID        NOT NULL REFERENCES benevole(id),
    creneau_id   UUID        NOT NULL REFERENCES creneau(id) ON DELETE CASCADE,
    statut       VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE' CHECK (statut IN ('EN_ATTENTE', 'CONFIRME', 'REFUSE', 'ANNULE')),
    commentaire  TEXT,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (benevole_id, creneau_id)
);

-- ------------------------------------------------------------
-- ACCREDITATION
-- ------------------------------------------------------------
CREATE TABLE accreditation (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    benevole_id          UUID        NOT NULL REFERENCES benevole(id),
    evenement_id         UUID        NOT NULL REFERENCES evenement(id) ON DELETE CASCADE,
    type                 VARCHAR(20) NOT NULL CHECK (type IN ('BENEVOLE', 'STAFF', 'PRESSE', 'ARTISTE')),
    code_qr              VARCHAR(255) UNIQUE,
    date_debut_validite  DATE,
    date_fin_validite    DATE,
    valide               BOOLEAN     NOT NULL DEFAULT FALSE,
    date_emission        TIMESTAMP,
    UNIQUE (benevole_id, evenement_id)
);

-- Zones d'accès d'une accréditation (relation 1-N)
CREATE TABLE accreditation_zones (
    accreditation_id UUID        NOT NULL REFERENCES accreditation(id) ON DELETE CASCADE,
    zone             VARCHAR(20) NOT NULL CHECK (zone IN ('GENERAL', 'SCENE', 'BACKSTAGE', 'VIP')),
    PRIMARY KEY (accreditation_id, zone)
);

-- ------------------------------------------------------------
-- JOURNAL_AUDIT (RGPD)
-- ------------------------------------------------------------
CREATE TABLE journal_audit (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    utilisateur_id UUID       NOT NULL REFERENCES utilisateur(id),
    action        VARCHAR(20) NOT NULL CHECK (action IN ('LECTURE', 'CREATION', 'MODIFICATION', 'SUPPRESSION', 'EXPORT', 'ANONYMISATION')),
    entite_cible  VARCHAR(50) NOT NULL,
    entite_id     UUID        NOT NULL,
    ip_address    VARCHAR(45),
    timestamp     TIMESTAMP   NOT NULL DEFAULT NOW(),
    detail        TEXT
);

-- ------------------------------------------------------------
-- INDEX
-- ------------------------------------------------------------
CREATE INDEX idx_utilisateur_email         ON utilisateur(email);
CREATE INDEX idx_benevole_email            ON benevole(email);
CREATE INDEX idx_mission_evenement         ON mission(evenement_id);
CREATE INDEX idx_creneau_mission           ON creneau(mission_id);
CREATE INDEX idx_affectation_benevole      ON affectation(benevole_id);
CREATE INDEX idx_affectation_creneau       ON affectation(creneau_id);
CREATE INDEX idx_accreditation_benevole    ON accreditation(benevole_id);
CREATE INDEX idx_accreditation_evenement   ON accreditation(evenement_id);
CREATE INDEX idx_journal_audit_utilisateur ON journal_audit(utilisateur_id);
CREATE INDEX idx_journal_audit_entite      ON journal_audit(entite_cible, entite_id);
