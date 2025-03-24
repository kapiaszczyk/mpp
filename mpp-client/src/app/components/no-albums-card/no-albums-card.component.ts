import { NgIf } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-no-albums-card',
  imports: [
    MatCardModule,
    TranslateModule,
    NgIf
  ],
  templateUrl: './no-albums-card.component.html',
  styleUrl: './no-albums-card.component.scss'
})
export class NoAlbumsCardComponent {

  @Input() noAlbumsSharedWithUser: boolean = false;

}
