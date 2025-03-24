import { NgFor } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { LogoutService } from '../../services/logout/logout.service';
import { ApiService } from '../../services/api/api.service';

@Component({
    selector: 'app-shared-menu',
    imports: [TranslateModule, NgFor, MatButtonModule],
    templateUrl: './shared-menu.component.html',
    styleUrl: './shared-menu.component.scss'
})
export class SharedMenuComponent {
  @Input() menuItems: { label: string, route: string, hidden?: boolean }[] = [];

  router = inject(Router);
  logoutService = inject(LogoutService);
  apiService = inject(ApiService);

  navigate(route: string): void {
    if (route === '/logout') {
      this.logoutService.logout();
      route = '/login';
    }
    else if (route === '/gallery') {
      this.apiService.getRootDomain().subscribe((domain) => {
        route = `/gallery/${domain.id}`;
        this.router.navigate([route]);
      });
    }
    this.router.navigate([route]);
  }

}
