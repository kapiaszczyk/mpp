import { Component, inject, Inject } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatDividerModule } from '@angular/material/divider';
import { MatRadioModule } from '@angular/material/radio';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';
import { Router } from '@angular/router';


@Component({
    selector: 'app-delete-domain-dialog',
    imports: [
        MatFormFieldModule,
        MatButtonModule,
        MatDialogModule,
        CommonModule,
        FormsModule,
        MatInputModule,
        MatDividerModule,
        MatRadioModule,
        MatTooltipModule,
        MatIconModule,
        TranslateModule
    ],
    templateUrl: './delete-domain-modal.component.html',
    styleUrl: './delete-domain-modal.component.scss'
})
export class DeleteDomainDialogComponent {
  // For now, the implementation does not include
  // Moving children albums to parent album
  moveChildrenDataToParent = false;
  moveToParentDomain = null;

  router = inject(Router);
  data = inject<{ albumName: string, parentAlbumId: string }>(MAT_DIALOG_DATA);

  areAllChoicesMade(): boolean {
    return this.moveChildrenDataToParent !== null && this.moveChildrenDataToParent !== undefined &&
           this.moveToParentDomain !== null && this.moveToParentDomain !== undefined;
  }
}
