import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ApiService } from '../../../services/api/api.service';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { UserStatistics } from '../../../models/statistics.model';
import { TranslateModule } from '@ngx-translate/core';
import { NotificationService } from '../../../services/notification/notification.service';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';

@Component({
  selector: 'app-admin-panel-user-dialog',
  imports: [
    MatButtonModule,
    CommonModule,
    TranslateModule,
    MatCardModule,
    MatListModule
  ],
  templateUrl: './admin-panel-user-dialog.component.html',
  styleUrl: './admin-panel-user-dialog.component.scss'
})
export class AdminPanelUserDialogComponent {
  userStatistics: UserStatistics;
  domainKeys: String[];
  isProcessing = false;

  constructor(
    public dialogRef: MatDialogRef<AdminPanelUserDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private apiService: ApiService,
    private notificationService: NotificationService
  ) {
    console.log(data);

    this.userStatistics = { ...data.userStatistics };
    this.domainKeys = Object.keys(this.userStatistics.albumStats);
  }

  updateUserRole(): void {
    this.isProcessing = true;
    const newRole = this.userStatistics.role === 'ADMIN' ? 'USER' : 'ADMIN';
    this.apiService.changeRole(this.userStatistics.userId, newRole).subscribe({
      next: () => {
        this.userStatistics.role = newRole;
        this.isProcessing = false;
        this.notificationService.sendSuccessMessage('notifications.roleUpdatedSuccess');
      },
      error: (error) => {
        this.isProcessing = false;
        this.notificationService.sendErrorMessage('notifications.roleUpdatedError');
      }
    });
  }

  deleteUser(): void {
    this.isProcessing = true;
    this.apiService.deleteUser(this.userStatistics.userId).subscribe({
      next: () => {
        this.dialogRef.close(true);
        this.notificationService.sendSuccessMessage('notifications.userDeletedSuccess');
      },
      error: (error) => {
        this.notificationService.sendErrorMessage('notifications.userDeletedError');
      }
    });

  }

  close(): void {
    this.dialogRef.close();
  }

  domainStatisticsArray(domainStatistics: Map<string, number>): [string, number][] {
    return Array.from(Object.entries(domainStatistics));
  }

}
