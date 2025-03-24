import { Component, inject, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ApiService } from '../../../services/api/api.service';
import { NgFor, NgIf } from '@angular/common';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { MatButton } from '@angular/material/button';
import { TranslateModule } from '@ngx-translate/core';
import { CompositeDomainInfo, DomainSharedUsersInfo } from '../../../models/composite-domain-info.model';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';

@Component({
  selector: 'app-domain-info-dialog',
  imports: [
    NgFor,
    NgIf,
    CommonModule,
    MatButton,
    MatDialogModule,
    TranslateModule,
    MatListModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatFormFieldModule
  ],
  templateUrl: './domain-info-dialog.component.html',
  styleUrl: './domain-info-dialog.component.scss'
})
export class DomainInfoDialogComponent implements OnInit {
  domainInfo: CompositeDomainInfo | null = null;
  loading: boolean = true;
  error: string = '';
  sharedUsers: DomainSharedUsersInfo[] = [];

  apiService = inject(ApiService);
  dialogRef = inject(MatDialogRef<DomainInfoDialogComponent>);
  data = inject<{ albumId: string }>(MAT_DIALOG_DATA);

  ngOnInit(): void {
    this.fetchDomainInfoAndSharedUsers();
  }

  fetchDomainInfoAndSharedUsers(): void {
    this.loading = true;

    forkJoin({
      domainInfo: this.apiService.getDomainInfo(this.data.albumId),
      sharedUsers: this.apiService.getSharedUsers(this.data.albumId)
    }).subscribe({
      next: ({ domainInfo, sharedUsers }) => {
        this.domainInfo = domainInfo;
        this.sharedUsers = sharedUsers;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error fetching domain or shared users information.';
        this.loading = false;
      }
    });
  }

  protected areThereSharedUsers(): boolean {
    return this.sharedUsers.length > 0;
  }

}
