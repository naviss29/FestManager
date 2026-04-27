-- Google OAuth : identifiant Google pour les comptes staff
ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS google_id VARCHAR(255) UNIQUE;

-- Magic link profil bénévole (auto-édition, valable 24h)
ALTER TABLE benevole ADD COLUMN IF NOT EXISTS profil_token VARCHAR(255) UNIQUE;
ALTER TABLE benevole ADD COLUMN IF NOT EXISTS profil_token_expiry TIMESTAMP;
