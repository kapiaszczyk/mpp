import { Component, inject, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ShareDomainDialogComponent } from '../../dialogs/share-domain-dialog/share-domain-dialog.component';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-share-album-button',
  imports: [
    MatIconModule,
    MatButtonModule,
    TranslateModule
  ],
  templateUrl: './share-album-button.component.html',
  styleUrl: './share-album-button.component.scss'
})
export class ShareAlbumButtonComponent {
  @Input() currentDomainId!: string;

  dialog = inject(MatDialog);

  shareAlbum(): void {
    this.dialog.open(ShareDomainDialogComponent, {
      data: { domainId: this.currentDomainId }
    });
  }
}
