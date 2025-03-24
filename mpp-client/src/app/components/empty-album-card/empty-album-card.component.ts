import { NgIf } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-empty-album-card',
  imports: [
    MatCardModule,
    TranslateModule,
    NgIf,
    MatIconModule
  ],
  templateUrl: './empty-album-card.component.html',
  styleUrl: './empty-album-card.component.scss'
})
export class EmptyAlbumCardComponent {

  @Input() canUserUploadPhotos: boolean = true;

}
