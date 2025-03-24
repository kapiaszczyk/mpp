import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-rename-album-dialog',
  imports: [
    MatFormFieldModule,
    MatButtonModule,
    MatDialogModule,
    CommonModule,
    FormsModule,
    MatInputModule,
    TranslateModule
  ],
  templateUrl: './rename-album-dialog.component.html',
  styleUrl: './rename-album-dialog.component.scss'
})
export class RenameAlbumDialogComponent {

  data = inject<{ name: string }>(MAT_DIALOG_DATA);

}
