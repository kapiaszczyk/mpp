import { Component, inject, Inject, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { NgFor, NgClass, NgIf } from '@angular/common';
import { MatIcon } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
    selector: 'app-breadcrumbs',
    imports: [NgFor, NgClass, NgIf, MatButtonModule, MatIcon, TranslateModule, MatTooltipModule],
    templateUrl: './breadcrumbs.component.html',
    styleUrl: './breadcrumbs.component.scss'
})
export class BreadcrumbsComponent {
  @Input() breadcrumbs: any[] = [];
  @Input() navigateToDomain!: (id: string) => void;

  router = inject(Router);

}
