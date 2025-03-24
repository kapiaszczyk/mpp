import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-no-tags-card',
  imports: [
    MatCardModule,
    TranslateModule
  ],
  templateUrl: './no-tags-card.component.html',
  styleUrl: './no-tags-card.component.scss'
})
export class NoTagsCardComponent {

}
