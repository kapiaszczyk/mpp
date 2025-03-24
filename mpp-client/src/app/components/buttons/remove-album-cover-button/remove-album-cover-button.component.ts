import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { ApiService } from '../../../services/api/api.service';
import { NotificationService } from '../../../services/notification/notification.service';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-remove-album-cover-button',
  imports: [
    MatIconModule,
    MatButtonModule,
    TranslateModule
  ],
  templateUrl: './remove-album-cover-button.component.html',
  styleUrl: './remove-album-cover-button.component.scss'
})
export class RemoveAlbumCoverButtonComponent {

  @Input() currentDomainId!: string;

  @Output() albumCoverRemoved = new EventEmitter<void>();

  apiService = inject(ApiService);
  notificationService = inject(NotificationService);

  protected removeAlbumCover(): void {
    this.apiService.removeAlbumCover(this.currentDomainId!).subscribe({
      next: () => {
        this.notificationService.sendSuccessMessage('notifications.albumCoverRemovedSuccess');
        this.albumCoverRemoved.emit();
      },
      error: () => {
        this.notificationService.sendErrorMessage('notifications.albumCoverRemovedError');
      },
    });
  }

}
