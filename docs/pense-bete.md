# FestManager — Pense-bête / Idées futures

> Idées et fonctionnalités à évaluer pour une future version. Rien ici n'est planifié — c'est une liste de capture pour ne rien oublier.

---

## ⚠️ Limitation Vitest + Angular build : vi.mock inopérant sur les services

Dans le runner `@angular/build:unit-test` (Angular 21), **`vi.mock()` ne s'applique pas aux modules importés dans les services compilés** par le pipeline Angular/esbuild. Il s'applique uniquement aux imports du fichier de test lui-même.

**Conséquences connues :**
- `vi.mock('jwt-decode')` → ignoré dans `AuthService` → utiliser un vrai JWT valide dans les tests
- `vi.mock('@stomp/rx-stomp')` → ignoré dans `WebSocketService` → tester le contrat public (retourne Observable, pas d'erreur) sans inspecter les internals
- `vi.mock('sockjs-client')` → même limitation

**Pattern recommandé pour les services Angular :**
```typescript
// ❌ Ne fonctionne pas pour les dépendances des services compilés
vi.mock('jwt-decode', () => ({ jwtDecode: vi.fn().mockReturnValue({...}) }));

// ✅ Utiliser un vrai JWT syntaxiquement valide à la place
const FAKE_JWT = 'eyJhbGciOiJIUzI1NiJ9.<payload_base64url>.fakesig';
// Généré avec : node -e "console.log(Buffer.from(JSON.stringify({sub:...})).toString('base64url'))"
```

**Pour les services instanciant une classe externe avec `new` :** tester le contrat public (retour Observable, absence d'erreur) plutôt que les appels internes.

---

## ⚠️ Règle obligatoire — Angular 21 : change detection après HTTP

**Tout nouveau composant qui fait un appel HTTP et met à jour la vue DOIT suivre ce pattern :**

```typescript
import { ChangeDetectorRef, Component } from '@angular/core';

@Component({ ... })
export class MonComposant {
  constructor(private cdr: ChangeDetectorRef) {}

  charger(): void {
    this.chargement = true;
    this.service.lister().subscribe({
      next: data => {
        this.donnees = data;
        this.chargement = false;
        this.cdr.detectChanges(); // ← NE PAS OUBLIER
      },
      error: () => { this.chargement = false; this.cdr.detectChanges(); }
    });
  }
}
```

**Pourquoi :** Angular 21 (mode NgModule non-standalone) exécute les callbacks HTTP hors zone Angular. Sans `detectChanges()`, le loader reste bloqué et les données ne s'affichent pas tant que l'utilisateur ne clique pas dans la page.

**Ce pattern est déjà appliqué sur tous les composants existants.** Ne pas l'omettre sur les nouveaux.

### Variante : bibliothèques JS externes (Google Identity Services, etc.)

Les callbacks enregistrés auprès de bibliothèques externes s'exécutent **hors zone Angular**. `detectChanges()` seul ne suffit pas — il faut aussi `NgZone.run()` :

```typescript
constructor(private ngZone: NgZone, private cdr: ChangeDetectorRef) {}

// Dans ngAfterViewInit ou équivalent
bibliothèque.onCallback = (data: any) =>
  this.ngZone.run(() => {
    this.etat = data;
    this.cdr.detectChanges();
  });
```

**Déjà appliqué sur :** `LoginComponent` et `RegisterComponent` (callback Google Identity Services).

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
- ~~Les images (bannière, affiche) nécessiteront un stockage fichier côté backend (ex : dossier `uploads/` ou bucket S3)~~ → **IMPLÉMENTÉ** (FichierService + photo bénévole + bannière événement)

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

## ✅ Portail self-service bénévole — IMPLÉMENTÉ (F24)

Les bénévoles peuvent accéder à leur profil via un **magic link** reçu par email :

1. Le bénévole entre son email sur `/mon-profil` → le backend génère un token UUID (24h) dans `benevole.profil_token` et envoie le lien par email
2. Le clic sur le lien ouvre `/mon-profil/connexion/{token}` → le frontend valide le token, stocke `fm_bvl_token` dans localStorage, redirige vers `/mon-profil/profil`
3. `/mon-profil/profil` est protégé par `BenevoleGuard` (canActivate) — redirige vers `/mon-profil` si pas de session
4. Sur le profil : édition (taille t-shirt, téléphone, compétences, disponibilités) + upload photo pour badge

**Services clés :**
- `BenevoleSessionService` — gestion du token localStorage (`fm_bvl_token`)
- `BenevoleGuard` — protection de la route `/mon-profil/profil`
- `BenevoleProfilService` — appels API (demander lien, GET/PUT profil, upload photo multipart)

---

## ✅ Connexion Google OAuth (staff) — IMPLÉMENTÉ (F23)

Les organisateurs et admins peuvent se connecter / s'inscrire via Google Identity Services.

**Fonctionnement :**
- Frontend : bouton GIS rendu via `google.accounts.id.renderButton()` dans `ngAfterViewInit()`
- Le callback reçoit un ID token (credential) → envoyé à `POST /api/auth/google`
- Backend : validation via `https://oauth2.googleapis.com/tokeninfo?id_token={credential}` (vérifie `aud`)
- 3 cas : `google_id` connu → connexion, email connu → liaison du compte + connexion, inconnu → création (admin si 1er compte, sinon en attente)

**Config requise :** variable `GOOGLE_CLIENT_ID` dans Railway (backend) et dans `environment.ts` (frontend).  
Si `GOOGLE_CLIENT_ID` est vide, le bouton Google est remplacé par un placeholder désactivé (aucune erreur JS).

---

## ✅ Génération de badges imprimables — IMPLÉMENTÉ

Générer un badge PDF prêt à imprimer pour chaque bénévole accrédité, aux couleurs et à l'identité visuelle de l'événement.

**Contenu du badge :**
- ~~Photo d'identité du bénévole (uploadée par le bénévole ou l'organisateur)~~ → **IMPLÉMENTÉ via portail self-service (F24)** : le bénévole uploade sa photo depuis `/mon-profil/profil`, stockée dans `uploads/benevoles/` et référencée dans `benevole.photo_url`
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
