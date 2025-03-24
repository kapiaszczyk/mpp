import { inject, Injectable } from '@angular/core';
import { ApiService } from '../api/api.service';
import { NotificationService } from '../notification/notification.service';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UtilService {

  apiService = inject(ApiService);
  notificationService = inject(NotificationService);

  setPhotoAsAlbumCover(
    albumId: string,
    photoId: string,
    refreshData: () => void
  ): Observable<void> {
    return this.apiService.setAsAlbumCover(albumId, photoId).pipe(
      tap({
        next: () => {
          this.notificationService.sendSuccessMessage('notifications.albumCoverSetSuccess');
          refreshData();
        },
        error: () => {
          this.notificationService.sendErrorMessage('notifications.albumCoverSetError');
        }
      })
    );
  }
}
