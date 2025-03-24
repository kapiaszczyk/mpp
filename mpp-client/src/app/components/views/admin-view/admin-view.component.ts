import { Component, inject } from '@angular/core';
import { ApiService } from '../../../services/api/api.service';
import { BehaviorSubject, forkJoin } from 'rxjs';
import { SystemStatistics, UserStatistics } from '../../../models/statistics.model';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { AdminPanelUserDialogComponent } from '../../dialogs/admin-panel-user-dialog/admin-panel-user-dialog.component';

import { MatDialog } from '@angular/material/dialog';
import { TranslateModule } from '@ngx-translate/core';
import { SharedMenuComponent } from '../../shared-menu/shared-menu.component';

@Component({
  selector: 'app-admin-view',
  imports: [
    MatButtonModule,
    MatToolbarModule,
    MatCardModule,
    MatSidenavModule,
    MatIconModule,
    MatMenuModule,
    MatDialogModule,
    CommonModule,
    TranslateModule,
    SharedMenuComponent
  ],
  templateUrl: './admin-view.component.html',
  styleUrl: './admin-view.component.scss'
})
export class AdminViewComponent {

  apiService = inject(ApiService);
  router = inject(Router)
  dialog = inject(MatDialog);

  private sidenavState = new BehaviorSubject<boolean>(false);
  sidenavState$ = this.sidenavState.asObservable();

  statistics: SystemStatistics | null = null;
  currentUserStatistics: UserStatistics | null = null;

  menuItems = [
    { label: 'mainMenu.mainGallery', route: '/gallery' },
    { label: 'mainMenu.tagView', route: '/tag-gallery' },
    { label: 'mainMenu.sharedAlbums', route: '/shared' },
    { label: 'mainMenu.adminView', route: '/admin-panel' },
    { label: 'mainMenu.logout', route: '/logout' },
  ];

  ngOnInit(): void {
    this.getStatistics();
  }

  getStatistics(): void {
    forkJoin({
      numberOfDomainsInSystem: this.apiService.getNumberOfDomainsInSystem(),
      numberOfPhotosInSystem: this.apiService.getNumberOfPhotosInSystem(),
      numberOfUsersInSystem: this.apiService.getNumberOfUsersInSystem(),
      rolesInSystem: this.apiService.getRolesInSystem(),
      usersInSystem: this.apiService.getUsersInSystem(),
      domainsInSystem: this.apiService.getDomainsInSystem(),
      spaceUsedInSystem: this.apiService.getSpaceUsedInSystem()
    }).subscribe({
      next: (stats: SystemStatistics) => {
        this.statistics = stats;
      },
      error: (error) => {
        console.error('Error fetching statistics:', error);
      },
      complete: () => {
      }
    });
  }

  logout() {
    this.apiService.logout().subscribe(() => {
      this.router.navigate(['/login']);
    });
  }

  navigateToGallery() {
    this.apiService.getRootDomain().subscribe({
      next: (response: any) => {
        this.router.navigate(['/gallery', response.id]);
      },
      error: (err) => {
        console.error(err);
      },
    });
  }


  navigateToTagGallery() {
    this.router.navigate(['/tag-gallery']);
  }

  navigateToRootShared() {
    this.router.navigate(['/shared']);
  }

  openUserModal(user: any): void {
    this.apiService.getStatisticsForUser(user.id).subscribe({
      next: (userStatistics: UserStatistics) => {
        const dialogRef = this.dialog.open(AdminPanelUserDialogComponent, {
          width: '400px',
          data: { userStatistics }
        });

        dialogRef.afterClosed().subscribe(() => {
          this.getStatistics();
        });
      },
      error: (error) => {
        console.error('Error fetching statistics for user', error);
      }
    });
  }

}
