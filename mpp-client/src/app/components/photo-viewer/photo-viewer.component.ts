import { Component, Input, Output, EventEmitter, HostListener, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { MatIcon } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../services/api/api.service';
import { IndexedDbService } from '../../services/indexeddb/indexed-db.service';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PhotoTagsComponent } from "./photo-tags/photo-tags.component";
import { PhotoMetadataWithBlobUrls } from '../../models/photo-metadata-with-urls.model';

@Component({
  selector: 'app-photo-viewer',
  imports: [CommonModule, TranslateModule, MatIcon, MatSidenavModule, MatButtonModule, MatProgressSpinnerModule, PhotoTagsComponent],
  templateUrl: './photo-viewer.component.html',
  styleUrl: './photo-viewer.component.scss'
})
export class PhotoViewerComponent {

  apiService = inject(ApiService);
  indexedDbService = inject(IndexedDbService);
  drawerOpen: boolean = false;
  isCurrentPhotoLoading: boolean = false;

  @Input() currentPhotoIndex: number = 0;
  @Input() photos: PhotoMetadataWithBlobUrls[] = [];
  @Input() editable: boolean = true;
  @Input() currentPhotoId: string | undefined;
  @Input() openedFromGallery: boolean = false;

  @Output() close = new EventEmitter<void>();

  toggleDrawer() {
    this.drawerOpen = !this.drawerOpen;
  }

  private async loadCurrentPhoto() {
    const cachedPhoto = await this.getCachedPhoto(this.currentPhotoId!);
    this.currentPhotoIndex = this.findPhotoIndexById(this.currentPhotoId!);

    if (cachedPhoto) {
      this.photos[this.currentPhotoIndex].fullUrl = cachedPhoto.fullUrl;
      this.isCurrentPhotoLoading = false;
    } else {
      this.isCurrentPhotoLoading = true;
      this.fetchAndCachePhoto(this.currentPhotoId!);
    }
  }

  private async fetchAndCachePhoto(photoId: string) {
    try {
      const photoBlob = await this.apiService.downloadPhoto(photoId).toPromise();
      const objectUrl = URL.createObjectURL(photoBlob);

      const db = await this.indexedDbService.getDb();
      await db.put('photos', { id: photoId, data: photoBlob });

      const photoIndex = this.photos.findIndex(photo => photo.id === this.currentPhotoId);

      this.photos[photoIndex].fullUrl = objectUrl;
      this.isCurrentPhotoLoading = false;
    } catch (error) {
      console.error('Error fetching photo:', error);
    }
  }

  private async getCachedPhoto(photoId: string) {
    const db = await this.indexedDbService.getDb();
    const cachedPhoto = await db.get('photos', photoId);
    if (cachedPhoto) {
      const objectUrl = URL.createObjectURL(cachedPhoto.data);
      return { id: photoId, fullUrl: objectUrl };
    }
    return null;
  }

  prevPhoto() {
    if (this.currentPhotoIndex > 0) {
      this.currentPhotoIndex--;
      this.currentPhotoId = this.photos[this.currentPhotoIndex].id;
      this.loadCurrentPhoto();
    }
  }

  nextPhoto() {
    if (this.currentPhotoIndex < this.photos.length - 1) {
      this.currentPhotoIndex++;
      this.currentPhotoId = this.photos[this.currentPhotoIndex].id;
      this.loadCurrentPhoto();
    }
  }

  closeViewer() {
    this.close.emit();
  }

  updateTags(photo: any, updatedTags: string[]) {
    photo.tags = updatedTags;
    this.apiService.updatePhotoTags(photo.id, updatedTags).subscribe();
  }

  private findPhotoIndexById(photoId: string) {
    return this.photos.findIndex(photo => photo.id === photoId);
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (event.key === 'ArrowLeft') {
      this.prevPhoto();
    } else if (event.key === 'ArrowRight') {
      this.nextPhoto();
    } else if (event.key === 'Escape') {
      this.closeViewer();
    } else if (event.key === 'i') {
      this.toggleDrawer();
    }
  }
}
