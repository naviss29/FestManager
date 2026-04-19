-- V3 : Stockage des fichiers (photos bénévoles, bannières événements)
ALTER TABLE benevole ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);
ALTER TABLE evenement ADD COLUMN IF NOT EXISTS banniere_url VARCHAR(500);
