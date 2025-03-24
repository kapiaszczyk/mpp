import { NgIf } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';

/**
 * Component of the photo selection bar.
 */
@Component({
  selector: 'app-photo-selection-bar',
  imports: [
    MatCheckboxModule,
    MatIconModule,
    TranslateModule,
    MatButtonModule,
    MatTooltipModule,
    NgIf
  ],
  templateUrl: './photo-selection-bar.component.html',
  styleUrl: './photo-selection-bar.component.scss'
})
export class PhotoSelectionBarComponent {
  @Input() totalPhotos: number = 0;
  @Input() selectedPhotosCount: number = 0;
  @Input() canDeletePhotos: boolean = true;
  @Input() canDownloadPhotos: boolean = true;
  @Input() canDuplicatePhotos: boolean = true;
  @Input() canMovePhotos: boolean = true;
  @Input() canSetAsAlbumCover: boolean = true;

  @Output() toggleSelectAll = new EventEmitter<boolean>();
  @Output() download = new EventEmitter<void>();
  @Output() move = new EventEmitter<void>();
  @Output() duplicate = new EventEmitter<void>();
  @Output() delete = new EventEmitter<void>();
  @Output() setAsAlbumCover = new EventEmitter<void>();

  selectAll() {
    const allSelected = this.selectedPhotosCount === this.totalPhotos;
    this.toggleSelectAll.emit(!allSelected);
  }
}
