import { Component, inject, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';
import { DomainInfoDialogComponent } from '../../dialogs/domain-info-dialog/domain-info-dialog.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-show-album-info-button',
  imports: [
    MatTooltipModule,
    MatButtonModule,
    MatIconModule,
    TranslateModule
  ],
  templateUrl: './show-album-info-button.component.html',
  styleUrl: './show-album-info-button.component.scss'
})
export class ShowAlbumInfoButtonComponent {

  @Input() currentDomainId!: string;

  dialog = inject(MatDialog);

  showAlbumInfo(): void {
    this.dialog.open(DomainInfoDialogComponent, {
      data: { albumId: this.currentDomainId }
    });
  }

}
