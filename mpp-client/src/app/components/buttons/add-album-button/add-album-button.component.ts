import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { NotificationService } from '../../../services/notification/notification.service';
import { ApiService } from '../../../services/api/api.service';
import { MatDialog } from '@angular/material/dialog';
import { CreateAlbumDialogComponent } from '../../dialogs/create-album-dialog/create-album-dialog.component';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { TranslateModule } from '@ngx-translate/core';

/**
 * Component displaying a button to add a new album.
 */
@Component({
  selector: 'app-add-album-button',
  imports: [
    MatIconModule,
    MatButtonModule,
    TranslateModule,
  ],
  templateUrl: './add-album-button.component.html',
  styleUrl: './add-album-button.component.scss'
})
export class AddAlbumButtonComponent {

  notificationService = inject(NotificationService);
  apiService = inject(ApiService);
  dialog = inject(MatDialog);

  @Input() currentDomainId!: string;
  @Output() albumCreated = new EventEmitter<void>();

  createAlbum(): void {
    const dialogRef = this.dialog.open(CreateAlbumDialogComponent, {
      data: { name: '' },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.apiService.createDomain(this.currentDomainId, result).subscribe({
          next: () => {
            this.notificationService.sendSuccessMessage('notifications.albumCreatedSuccess');
            this.albumCreated.emit();
          },
          error: () => {
            this.notificationService.sendErrorMessage('notifications.albumCreatedError');
          },
        });
      }
    });
  }

}
