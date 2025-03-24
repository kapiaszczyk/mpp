import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService } from '../../../services/api/api.service';
import { MatButtonModule } from '@angular/material/button';
import { catchError, forkJoin, of } from 'rxjs';
import { NotificationService } from '../../../services/notification/notification.service';

@Component({
  selector: 'app-upload-button',
  imports: [MatIcon, TranslateModule, MatTooltipModule, MatButtonModule],
  templateUrl: './upload-button.component.html',
  styleUrl: './upload-button.component.scss'
})
export class UploadButtonComponent {

  apiService = inject(ApiService);
  notificationService = inject(NotificationService);

  @Input() event!: Event;
  @Input() domainId: any;
  @Output() done = new EventEmitter<void>();
  @Output() started = new EventEmitter<void>();

  uploadPhotos(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;
    let errors = 0;

    this.started.emit();
  
    const files = Array.from(input.files);
    const uploadRequests = files.map((file) =>
      this.apiService.uploadPhoto(this.domainId, file).pipe(
        catchError(() => {
          errors++;
          return of(null);
        })
      )
    );
  
    forkJoin(uploadRequests).subscribe({
      next: () => {
        if (errors === files.length) {
          this.notificationService.sendErrorMessage('notifications.photoUploadedError');
        }
        else if (errors > 0) {
          this.notificationService.sendErrorMessage('notifications.photoUploadedPhotosFailed');
        }
        else {
          this.notificationService.sendSuccessMessage('notifications.photoUploadedSuccess');
        }
        this.done.emit();
      },
    });
  }

}
