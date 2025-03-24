import { Component, inject, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MatFormField } from '@angular/material/form-field';
import { MatList } from '@angular/material/list';
import { MatLabel } from '@angular/material/form-field';
import { MatDialogModule } from '@angular/material/dialog';
import { MatListModule } from '@angular/material/list';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { ApiService } from '../../../services/api/api.service';
import { CreateAlbumDialogComponent } from '../create-album-dialog/create-album-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { TranslateModule } from '@ngx-translate/core';
import { NotificationService } from '../../../services/notification/notification.service';

@Component({
  selector: 'app-duplicate-photo-dialog',
  imports: [
    CommonModule,
    MatFormField,
    MatList,
    MatLabel,
    MatDialogModule,
    FormsModule,
    MatListModule,
    MatInputModule,
    MatButton,
    TranslateModule
  ],
  templateUrl: './duplicate-photo-dialog.component.html',
  styleUrl: './duplicate-photo-dialog.component.scss'
})
export class DuplicatePhotoDialogComponent {
  domains: any[] = [];
  filteredDomains: any[] = [];
  searchQuery = '';
  selectedDomain: any = null;
  creationDialogRef: any;
  currentDomainId: any;

  apiService = inject(ApiService);
  notificationService = inject(NotificationService);
  dialog = inject(MatDialog);
  dialogRef = inject<MatDialogRef<DuplicatePhotoDialogComponent>>(MatDialogRef);
  data = inject<{ domains: any[], currentDomainId: string }>(MAT_DIALOG_DATA);

  constructor(
  ) {
    this.domains = this.data.domains;
    this.filteredDomains = this.domains;
    this.currentDomainId = this.data.currentDomainId;
  }

  filterDomains(): void {
    this.filteredDomains = this.domains.filter(domain =>
      domain.name.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
    if (this.filteredDomains.length === 0 && this.searchQuery.length > 0) {
      this.selectedDomain = null;
    }
  }

  loadDomains(): void {
    this.apiService.getDomains().subscribe((domains) => {
      this.filteredDomains = domains;
      this.domains = domains;
    });
  }


  selectDomain(domain: any) {
    if (this.selectedDomain === domain) {
      this.selectedDomain = null;
    } else {
      this.selectedDomain = domain;
    }
  }

  createNewDomain(): void {
    const dialogRef = this.dialog.open(CreateAlbumDialogComponent, {
      width: '300px',
      data: { name: '' },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.apiService.createDomain(this.data.currentDomainId, result).subscribe({
          next: (response: any) => {
            this.loadDomains();
            this.selectedDomain = response;
            this.notificationService.sendSuccessMessage('notifications.albumCreatedSuccess');
          },
          error: (err) => {
            this.notificationService.sendErrorMessage('notifications.albumCreatedError');
          },
        });
      }
    });
  }
}
