import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ApiService } from '../../../services/api/api.service';
import { NotificationService } from '../../../services/notification/notification.service';
import { DeleteDomainDialogComponent } from '../../dialogs/delete-domain-dialog/delete-domain-modal.component';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { TranslateModule } from '@ngx-translate/core';
import { Domain } from '../../../models/domain.model';

@Component({
  selector: 'app-delete-album-button',
  imports: [
    MatIconModule,
    MatButtonModule,
    TranslateModule
  ],
  templateUrl: './delete-album-button.component.html',
  styleUrl: './delete-album-button.component.scss'
})
export class DeleteAlbumButtonComponent {
  @Input() currentDomainId!: string;
  @Input() currentDomainData: Domain | null = null;
  @Output() albumDeleted = new EventEmitter<void>();

  apiService = inject(ApiService);
  dialog = inject(MatDialog);
  router = inject(Router);
  notificationService = inject(NotificationService);

  deleteAlbum(): void {
    const dialogRef = this.dialog.open(DeleteDomainDialogComponent, {
      data: { albumName: this.currentDomainData?.name, parentAlbumId: this.currentDomainData?.parentId },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        const { moveChildrenDataToParent, moveToParentDomain } = result;
        this.apiService.deleteDomain(this.currentDomainId, moveChildrenDataToParent, moveToParentDomain).subscribe({
          next: () => {
            this.router.navigate(['/gallery', this.currentDomainData?.parentId]);
            this.notificationService.sendSuccessMessage('notifications.albumDeletedSuccess');
            this.albumDeleted.emit();
          },
          error: () => {
            this.notificationService.sendErrorMessage('notifications.albumDeletedError');
          },
        });
      }
    });
  }
}
