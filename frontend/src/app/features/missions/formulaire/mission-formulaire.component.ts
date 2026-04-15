import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { MissionService } from '../services/mission.service';
import { Mission, CATEGORIES_SUGGESTIONS } from '../models/mission.model';
import { OrganisationService } from '../../organisations/services/organisation.service';
import { Organisation } from '../../organisations/models/organisation.model';

export interface MissionFormulaireData {
  mission: Mission | null;
  evenementId: string;
}

@Component({
  selector: 'app-mission-formulaire',
  templateUrl: './mission-formulaire.component.html',
  standalone: false
})
export class MissionFormulaireComponent implements OnInit {

  form: FormGroup;
  chargement = false;
  estModification: boolean;
  organisations: Organisation[] = [];

  categoriesSuggestions = CATEGORIES_SUGGESTIONS;
  categoriesFiltrees$!: Observable<string[]>;

  constructor(
    private fb: FormBuilder,
    private missionService: MissionService,
    private organisationService: OrganisationService,
    private dialogRef: MatDialogRef<MissionFormulaireComponent>,
    @Inject(MAT_DIALOG_DATA) public data: MissionFormulaireData
  ) {
    const m = data.mission;
    this.estModification = !!m;
    this.form = this.fb.group({
      nom:                      [m?.nom ?? '',          [Validators.required, Validators.maxLength(255)]],
      description:              [m?.description ?? ''],
      lieu:                     [m?.lieu ?? '',         Validators.maxLength(255)],
      materielRequis:           [m?.materielRequis ?? ''],
      categorie:                [m?.categorie ?? '',    [Validators.required, Validators.maxLength(100)]],
      nbBenevolesRequis:        [m?.nbBenevolesRequis ?? 1, [Validators.required, Validators.min(1)]],
      multiAffectationAutorisee:[m?.multiAffectationAutorisee ?? false],
      gereeParOrganisation:     [m?.gereeParOrganisation ?? false],
      organisationId:           [m?.organisationId ?? null]
    });
  }

  ngOnInit(): void {
    this.categoriesFiltrees$ = this.form.get('categorie')!.valueChanges.pipe(
      startWith(''),
      map(val => {
        const filtre = (val ?? '').toLowerCase();
        return this.categoriesSuggestions.filter(c => c.toLowerCase().includes(filtre));
      })
    );

    this.organisationService.lister(0, 100).subscribe(page => {
      this.organisations = page.content;
    });

    this.form.get('gereeParOrganisation')!.valueChanges.subscribe((val: boolean) => {
      const ctrl = this.form.get('organisationId')!;
      if (val) {
        ctrl.setValidators(Validators.required);
      } else {
        ctrl.clearValidators();
        ctrl.setValue(null);
      }
      ctrl.updateValueAndValidity();
    });
  }

  get gereeParOrganisation(): boolean {
    return this.form.get('gereeParOrganisation')!.value;
  }

  soumettre(): void {
    if (this.form.invalid || this.chargement) return;
    this.chargement = true;

    const v = this.form.value;
    const request = {
      nom:                      v.nom,
      description:              v.description || undefined,
      lieu:                     v.lieu || undefined,
      materielRequis:           v.materielRequis || undefined,
      categorie:                v.categorie,
      nbBenevolesRequis:        v.nbBenevolesRequis,
      multiAffectationAutorisee:v.multiAffectationAutorisee,
      gereeParOrganisation:     v.gereeParOrganisation,
      organisationId:           v.gereeParOrganisation ? v.organisationId : undefined
    };

    const op$ = this.estModification
      ? this.missionService.modifier(this.data.mission!.id, request)
      : this.missionService.creer(this.data.evenementId, request);

    op$.subscribe({
      next: mission => this.dialogRef.close(mission),
      error: () => { this.chargement = false; }
    });
  }

  annuler(): void {
    this.dialogRef.close(null);
  }
}
