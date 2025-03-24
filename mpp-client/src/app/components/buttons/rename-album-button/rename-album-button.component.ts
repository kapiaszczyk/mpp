import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';
import { RenameAlbumDialogComponent } from '../../dialogs/rename-album-dialog/rename-album-dialog.component';
import { ApiService } from '../../../services/api/api.service';
import { NotificationService } from '../../../services/notification/notification.service';

@Component({
  selector: 'app-rename-album-button',
  imports: [
    MatIconModule,
    MatTooltipModule,
    MatButtonModule,
    TranslateModule,
    
  ],
  templateUrl: './rename-album-button.component.html',
  styleUrl: './rename-album-button.component.scss'
})
export class RenameAlbumButtonComponent {

  @Input() albumId!: string;
  @Input() albumName!: string;
  @Output() albumRenamed = new EventEmitter<void>();

  dialog = inject(MatDialog);
  notificationService = inject(NotificationService);
  apiService = inject(ApiService);

  renameAlbum(): void {
    const dialogRef = this.dialog.open(RenameAlbumDialogComponent, {
      data: { albumId: this.albumId, albumName: this.albumName },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.apiService.renameAlbum(this.albumId, result).subscribe({
          next: () => {
            this.notificationService.sendSuccessMessage('notifications.albumRenamedSuccess');
            this.albumRenamed.emit();
          },
          error: () => {
            this.notificationService.sendErrorMessage('notifications.albumRenamedError');
          }
        });
      }
    });

  
  }

}
