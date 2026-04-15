-- Migration V2 : catégorie de mission libre (suppression du CHECK enum)
-- La contrainte CHECK limitait les valeurs à la liste enum d'origine.
-- On la supprime pour permettre des catégories personnalisées (ex: BAR, SCENE-A...).

ALTER TABLE mission
    DROP CONSTRAINT IF EXISTS mission_categorie_check;

ALTER TABLE mission
    ALTER COLUMN categorie TYPE VARCHAR(100);
