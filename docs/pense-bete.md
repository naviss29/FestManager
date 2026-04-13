# FestManager — Pense-bête / Idées futures

> Idées et fonctionnalités à évaluer pour une future version. Rien ici n'est planifié — c'est une liste de capture pour ne rien oublier.

---

## Personnalisation cosmétique par événement

Permettre à chaque organisateur de personnaliser l'apparence de son événement dans l'application.

**Éléments à personnaliser :**
- Bannière de l'événement (image d'en-tête)
- Couleur principale (thème de couleur appliqué aux pages de l'événement)
- Affiche officielle (image affichée sur la page publique de l'événement)
- Running order (programme / ordre de passage des artistes ou activités)

**Notes :**
- Le running order pourrait être un simple fichier PDF uploadé, ou une saisie structurée (artiste / scène / horaire)
- La couleur principale pourrait être un color picker avec fallback sur la couleur par défaut de l'app
- Les images (bannière, affiche) nécessiteront un stockage fichier côté backend (ex : dossier `uploads/` ou bucket S3)

---

## Documents officiels de l'événement

Permettre à l'organisateur d'attacher des documents à un événement, consultables par les bénévoles depuis leur espace.

**Documents envisagés :**
- **Charte du bénévole** : engagements, code de conduite, droits et devoirs
- **Règlement intérieur de l'événement** : règles spécifiques à l'édition (accès zones, comportement, sécurité)

**Notes :**
- Format PDF recommandé (upload par l'organisateur)
- Possibilité de demander une **validation explicite** du bénévole à la lecture (case à cocher + date de validation tracée en base, dans la logique RGPD déjà en place)
- Pourrait être intégré au flux d'inscription : le bénévole ne peut pas finaliser son inscription sans avoir accepté la charte

---

## Génération de badges imprimables

Générer un badge PDF prêt à imprimer pour chaque bénévole accrédité, aux couleurs et à l'identité visuelle de l'événement.

**Contenu du badge :**
- Photo d'identité du bénévole (uploadée par le bénévole ou l'organisateur)
- Prénom / Nom
- Rôle / Type d'accréditation (Bénévole, Staff, Presse, Artiste)
- Zones d'accès autorisées
- QR code de l'accréditation (déjà généré — T03-01)
- Nom et logo/affiche de l'événement
- Dates de validité

**Identité visuelle de l'événement :**
- Couleur principale de l'événement (voir section "Personnalisation cosmétique") appliquée au fond ou à la bordure du badge
- Logo / affiche de l'événement en en-tête du badge
- Typographie cohérente avec la charte graphique de l'événement

**Notes techniques :**
- Génération PDF côté backend via une librairie Java (ex : iText ou Apache PDFBox)
- Endpoint dédié : `GET /api/accreditations/{id}/badge` → retourne un PDF
- Génération en lot : `GET /api/evenements/{id}/badges` → ZIP de tous les badges de l'événement
- La photo du bénévole nécessite un stockage fichier (même infrastructure que les images d'événement)
- Format badge recommandé : carte CR80 (85,6 × 54 mm) ou A6 selon usage (badge lanyard vs badge poche)

---
