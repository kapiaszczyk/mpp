import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ApiService } from '../../../services/api/api.service';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatMenuModule } from '@angular/material/menu';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSelectModule } from '@angular/material/select';
import { NgFor, NgIf } from '@angular/common';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { NotificationService } from '../../../services/notification/notification.service';
import { MatAutocompleteModule } from '@angular/material/autocomplete';

@Component({
  selector: 'app-share-domain-dialog',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatSidenavModule,
    MatIconModule,
    MatToolbarModule,
    MatMenuModule,
    MatCheckboxModule,
    MatDialogModule,
    MatSelectModule,
    NgFor,
    NgIf,
    MatDividerModule,
    MatListModule,
    FormsModule,
    MatInputModule,
    MatFormFieldModule,
    MatTooltipModule,
    TranslateModule,
    MatAutocompleteModule
  ],
  templateUrl: './share-domain-dialog.component.html',
  styleUrl: './share-domain-dialog.component.scss'
})
export class ShareDomainDialogComponent {
  sharedUsers: any[] = [];
  searchUsername: any = null;
  selectedUser: any = null;
  newUserPermission: string = 'VIEWER';
  filteredUsers: any[] = [];
  availableRoles = [
    { value: 'VIEWER', label: 'permissions.viewer' },
    { value: 'EDITOR', label: 'permissions.editor' },
    { value: 'ADMINISTRATOR', label: 'permissions.administrator' }
  ];

  isLoading: boolean = false;

  private apiService = inject(ApiService);
  private notificationService = inject(NotificationService);
  private data = inject<{ domainId: string }>(MAT_DIALOG_DATA);
  private translateService = inject(TranslateService);

  ngOnInit() {
    this.loadSharedUsers();
  }

  loadSharedUsers() {
    this.apiService.getSharedUsers(this.data.domainId).subscribe({
      next: (users: any[]) => {
        this.sharedUsers = users.map(user => ({
          ...user,
          translatedPermission: this.getTranslatedRole(user.permission)
        }));
      },
      error: (err) => console.error('Error loading shared users', err),
    });
  }

  getTranslatedRole(permission: string): string {
    const role = this.availableRoles.find(role => role.value === permission);
    return role ? this.translateService.instant(role.label) : permission;
  }

  searchUsers() {
    if (this.searchUsername.length < 2 && this.selectedUser === null) {
      this.filteredUsers = [];
      return;
    }

    this.apiService.searchUsers(this.searchUsername).subscribe({
      next: (users: any[]) => {
        this.filteredUsers = users;
      },
      error: () => {
        this.filteredUsers = [];
      },
    });

    console.log('searchUsers', this.searchUsername);
  }

  selectUser(user: any) {
    this.selectedUser = user;
    this.searchUsername = user;
    this.filteredUsers = [];
    console.log('selectUser', user);
  }

  addUserPermission() {
    if (!this.selectedUser || !this.newUserPermission) return;

    this.apiService.addSharedUser(this.data.domainId, this.selectedUser.id, this.newUserPermission).subscribe({
      next: () => {
        this.loadSharedUsers();
        this.searchUsername = '';
        this.selectedUser = null;
        this.newUserPermission = 'viewer';
        this.notificationService.sendSuccessMessage('notifications.permissionAddedSuccess');
      },
      error: () => {
        this.notificationService.sendErrorMessage('notifications.permissionAddedError');
      },
    });
  }

  updatePermission(user: any): void {
    this.apiService.updateUserPermission(this.data.domainId, user.id, user.permission).subscribe({
      next: () => {
        this.notificationService.sendSuccessMessage('notifications.permissionUpdatedSuccess');
        this.loadSharedUsers();
      },
      error: () => {
        this.notificationService.sendErrorMessage('notifications.permissionUpdatedError');
      },
    });
  }


  removeUserPermission(user: any): void {
    this.apiService.removeUserPermission(this.data.domainId, user.id).subscribe({
      next: (response) => {
        if (response.status === 200) {
          this.notificationService.sendSuccessMessage('notifications.permissionRemovedSuccess');
          this.loadSharedUsers();
        } else {
          this.notificationService.sendErrorMessage('notifications.permissionRemovedError');
        }
      },
      error: (err) => {
        if (err.status === 400) {
          this.notificationService.sendErrorMessage(err.error || 'notifications.unexpectedError');
        } else if (err.status === 500) {
          this.notificationService.sendErrorMessage('notifications.serverError');
        } else {
          this.notificationService.sendErrorMessage('notifications.unexpectedError');
        }
      },
    });
  }

  displayUserFn(user: any): string {
    return user && typeof user === 'object' ? user.username : '';
  }
  
  
}
