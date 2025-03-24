import { Component, inject, Inject } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-create-album-dialog',
  imports: [
    MatFormFieldModule,
    MatButtonModule,
    MatDialogModule,
    CommonModule,
    FormsModule,
    MatInputModule,
    TranslateModule
  ],
  templateUrl: './create-album-dialog.component.html',
  styleUrl: './create-album-dialog.component.scss'
})
export class CreateAlbumDialogComponent {
  data = inject<{ name: string }>(MAT_DIALOG_DATA);
}
