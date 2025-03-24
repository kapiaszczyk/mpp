import { Component, HostListener, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatMenuModule } from '@angular/material/menu';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ApiService } from '../../../services/api/api.service';
import { BehaviorSubject, map, Observable, startWith } from 'rxjs';
import { PhotoViewerComponent } from '../../photo-viewer/photo-viewer.component';
import { IndexedDbService } from '../../../services/indexeddb/indexed-db.service';
import { TranslateModule } from '@ngx-translate/core';
import { SharedMenuComponent } from '../../shared-menu/shared-menu.component';
import { PhotoCardsComponent } from '../../photo-cards/photo-cards.component';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormControl, FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { PhotosGroupedByAlbum } from '../../../models/photos-grouped-by-domain.model';
import { PhotoMetadataWithBlobUrls } from '../../../models/photo-metadata-with-urls.model';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { ReactiveFormsModule } from '@angular/forms';
import { PhotoSelectionBarComponent } from '../../photo-selection-bar/photo-selection-bar.component';
import { MovePhotosDialogComponent } from '../../dialogs/move-photos-dialog/move-photos-dialog.component';
import { NotificationService } from '../../../services/notification/notification.service';
import { DuplicatePhotoDialogComponent } from '../../dialogs/duplicate-photo-dialog/duplicate-photo-dialog.component';
import { ClickableTagsComponent } from "../../clickable-tags/clickable-tags.component";
import { NoTagsCardComponent } from '../../no-tags-card/no-tags-card.component';

@Component({
  selector: 'app-tag-gallery',
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatSidenavModule,
    MatIconModule,
    MatToolbarModule,
    MatMenuModule,
    MatCheckboxModule,
    MatDialogModule,
    PhotoViewerComponent,
    TranslateModule,
    SharedMenuComponent,
    PhotoCardsComponent,
    MatFormFieldModule,
    FormsModule,
    MatInputModule,
    MatAutocompleteModule,
    ReactiveFormsModule,
    PhotoSelectionBarComponent,
    ClickableTagsComponent,
    NoTagsCardComponent
],
  templateUrl: './tag-gallery.component.html',
  styleUrl: './tag-gallery.component.scss'
})
export class TagGalleryComponent {

  currentTag: string | null = null;
  searchControl = new FormControl('');
  tags: string[] = [];
  filteredTags!: Observable<string[]>;
  private sidenavState = new BehaviorSubject<boolean>(false);
  sidenavState$ = this.sidenavState.asObservable();
  isPhotoViewerOpen = false;
  photosGroupedByDomainResponse: PhotosGroupedByAlbum[] = [];
  selectedPhotoIndex = 0;
  selectedPhotoId = "";
  selectedPhotos = new Set<string>();
  hoveredPhoto: string | null = null;
  isSearching = false;
  menuItems: any;

  route = inject(ActivatedRoute);
  router = inject(Router);
  apiService = inject(ApiService);
  indexedDbService = inject(IndexedDbService);
  dialog = inject(MatDialog);
  notificationService = inject(NotificationService);

  rootDomainId: string = '';

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['tag']) {
        this.currentTag = params['tag'];
        this.fetchPhotos(this.currentTag!);
      } else {
        this.currentTag = null;
        this.photosGroupedByDomainResponse = [];
      }
    });
    this.loadTags();
    this.fetchRootDomainId();
    this.filteredTags = this.searchControl.valueChanges.pipe(
      startWith(''),
      map(value => this.filterTags(value || '').slice(0, 10))
    );
    this.apiService.isReady.subscribe(() => {
      this.menuItems = [
        { label: 'mainMenu.mainGallery', route: '/gallery' },
        { label: 'mainMenu.tagView', route: '/tag-gallery' },
        { label: 'mainMenu.sharedAlbums', route: '/shared' },
        { label: 'mainMenu.adminView', route: '/admin-panel', hidden: !this.canAccess('admin') },
        { label: 'mainMenu.logout', route: '/logout' },
      ];
    });
  }

  navigateToDomain(domainId: string): void {
    this.router.navigate(['/gallery', domainId]);
  }

  async fetchPhotos(tag: string): Promise<void> {
    this.photosGroupedByDomainResponse = [];
    const db = await this.indexedDbService.getDb();

    if (!tag) {
      console.error('No tag provided');
      return;
    }

    try {
      const metadata: PhotosGroupedByAlbum[] = await this.apiService.getPhotosByTagGroupedByDomain(tag).toPromise();

      this.photosGroupedByDomainResponse = await Promise.all(metadata.map(async (value: { albumName: string; albumId: string, photos: PhotoMetadataWithBlobUrls[]; }) => ({
        albumName: value.albumName,
        albumId: value.albumId,
        photos: await Promise.all(value.photos.map(async (photo) => {
          const cachedThumbnail = await db.get('thumbnails', photo.id);
          if (cachedThumbnail) {
            return { ...photo, url: URL.createObjectURL(cachedThumbnail.data) };
          }

          const thumbnailBlob = await this.apiService.downloadPhotoThumbnail(photo.id).toPromise();
          await db.put('thumbnails', { id: photo.id, data: thumbnailBlob });

          return { ...photo, url: URL.createObjectURL(thumbnailBlob) };
        }))
      })));
    } catch (error) {
      console.error('Error fetching photos:', error);
    }
  }

  loadTags(): void {
    this.apiService.getTags().subscribe((tags: any) => {
      this.tags = tags;
    });
  }

  navigateToTag(tag: string) {
    this.router.navigate(['/tag-gallery/', tag]);
  }

  openPhotoViewer(photoId: string) {
    this.selectedPhotoId = photoId;
    this.selectedPhotoIndex = this.photosGroupedByDomainResponse.flatMap((group) => group.photos).findIndex((photo) => photo.id === photoId);
    this.isPhotoViewerOpen = true;
  }

  closePhotoViewer() {
    this.isPhotoViewerOpen = false;
  }

  async viewPhoto(photoId: string) {
    const selectedPhoto = this.findPhotoById(photoId)!;

    if (!selectedPhoto.fullUrl) {
      const fullPhotoBlob = await this.apiService.downloadPhoto(selectedPhoto.id).toPromise();
      const db = await this.indexedDbService.getDb();
      await db.put('photos', { id: selectedPhoto.id, data: fullPhotoBlob });
      selectedPhoto.fullUrl = URL.createObjectURL(fullPhotoBlob);
    }
    this.selectedPhotoId = photoId;
    this.selectedPhotoIndex = this.photosGroupedByDomainResponse.flatMap((group) => group.photos).findIndex((photo) => photo.id === photoId);
    this.isPhotoViewerOpen = true;
  }

  downloadPhotos(): void {
    this.selectedPhotos.forEach((photoId) => {
      const photo = this.findPhotoById(photoId);
      if (!photo) {
        console.error(`Photo ${photoId} not found in metadata`);
        return;
      }

      const downloadUrl = `/photos/download/${photoId}`;

      // Use an anchor element to trigger the download
      this.apiService.getPhotoDownloadUrl(photoId).subscribe({
        next: (photoBlob) => {
          const blob = new Blob([photoBlob], { type: photoBlob.type });
          const url = window.URL.createObjectURL(blob);

          const a = document.createElement('a');
          a.href = url;
          a.download = `${photo.filename}`;
          document.body.appendChild(a);
          a.click();

          // Clean up
          document.body.removeChild(a);
          window.URL.revokeObjectURL(url);
        },
        error: (err) => {
          console.error(`Error downloading photo ${photoId}:`, err);
        },
      });
    });
  }

  selectedPhotosCount(): number {
    return this.selectedPhotos.size;
  }

  private filterTags(value: string): string[] {
    const filterValue = value.toLowerCase();
    return this.tags.filter((tag: string) => tag.toLowerCase().includes(filterValue));
  }

  canAccess(role: string): boolean {
    return this.apiService.canAccess(role);
  }

  searchByTag(tag: string): void {
    if (tag.trim()) {
      this.router.navigate(['/tag-gallery/', tag]);
    }
  }

  // FIXME: This makes the UI go to hell
  // @HostListener('document:keydown.enter', ['$event'])
  // handleEnter(event: KeyboardEvent) {
  //   if (this.selectedPhotos.size === 0 && this.photosGroupedByDomainResponse.length > 0 && !this.isSearching && !this.isPhotoViewerOpen) {
  //     // Open for the first photo in the list
  //     this.selectedPhotoId = this.photosGroupedByDomainResponse[0].photos[0].id;
  //     this.selectedPhotoIndex = 0;
  //     // Wait for the photo to be loaded
  //     this.fetchAndCacheFirstPhoto();
  //     this.openPhotoViewer(this.selectedPhotoId);
  //   }
  // }

  private findPhotoById(photoId: string): PhotoMetadataWithBlobUrls | undefined {
    return this.photosGroupedByDomainResponse.flatMap((group) => group.photos).find((photo) => photo.id === photoId);
  }

  protected mergeAllPhotosInOneArray(): PhotoMetadataWithBlobUrls[] {
    return this.photosGroupedByDomainResponse.flatMap((group) => group.photos);
  }

  // TODO: This is a terrible hack to fetch the first photo in the list and cache it
  private async fetchAndCacheFirstPhoto() {
    const firstPhoto = this.mergeAllPhotosInOneArray()[0];
    if (!firstPhoto) {
      return;
    }

    try {
      const photoBlob = await this.apiService.downloadPhoto(firstPhoto.id).toPromise();
      const db = await this.indexedDbService.getDb();
      const objectUrl = URL.createObjectURL(photoBlob);

      // Save the url in the photo metadata
      this.photosGroupedByDomainResponse.flatMap((group) => group.photos).find((photo) => photo.id === firstPhoto.id)!.fullUrl = objectUrl;

      await db.put('photos', { id: firstPhoto.id, data: photoBlob });
    } catch (error) {
      console.error('Error fetching photo:', error);
    }
  }

  areAllPhotosSelected(): boolean {
    return this.selectedPhotos.size === this.mergeAllPhotosInOneArray().length;
  }

  togglePhotoSelection(photoId: string, isSelected: boolean): void {
    if (isSelected) {
      this.selectedPhotos.add(photoId);
    } else {
      this.selectedPhotos.delete(photoId);
    }
  }

  toggleSelectAll(allSelected: boolean): void {
    if (this.areAllPhotosSelected()) {
      this.mergeAllPhotosInOneArray().forEach((photo) => {
        this.togglePhotoSelection(photo.id, false);
      });
    } else {
      // Need to traverse photosGroupedByDomainResponse and mark them as selected
      this.mergeAllPhotosInOneArray().forEach((photo) => {
        this.togglePhotoSelection(photo.id, true);
      });
    }
  }

  isAtLeastOnePhotoSelected(): boolean {
    return this.selectedPhotos.size > 0;
  }

  protected openMoveDialog(): void {
    this.apiService.getDomains().subscribe((domains) => {
      const dialogRef = this.dialog.open(MovePhotosDialogComponent, {
        width: '400px',
        data: {
          domains: domains,
          currentDomainId: this.rootDomainId,
          selectedPhotos: this.selectedPhotos,
        },
      });

      dialogRef.afterClosed().subscribe((selectedDomain) => {
        if (selectedDomain) {
          this.movePhotos(Array.from(this.selectedPhotos), selectedDomain.id);
        }
      });
    });
  }

  private movePhotos(photoIds: string[], targetDomainId: string): void {
    let successCount = 0;
    let errorCount = 0;

    photoIds.forEach((photoId, index) => {
      this.apiService.movePhoto(photoId, targetDomainId).subscribe({
        next: () => {
          successCount++;
        },
        error: (err) => {
          errorCount++;
          if (index === photoIds.length - 1) {
            this.notificationService.sendErrorMessage('notifications.photoMovedSomeFailed');
          }
        },
        complete: () => {
          if (index === photoIds.length - 1 && errorCount === 0) {
            this.notificationService.sendSuccessMessage('notifications.photoMovedSuccess');
          }
        }
      });
    });
  }

  protected openDuplicateDialog(): void {
    this.apiService.getDomains().subscribe((domains) => {
      const dialogRef = this.dialog.open(DuplicatePhotoDialogComponent, {
        data: {
          domains: domains,
          currentDomainId: this.rootDomainId,
          selectedPhotos: this.selectedPhotos,
          isDuplicate: true,
        },
      });

      dialogRef.afterClosed().subscribe((selectedDomain) => {
        if (selectedDomain) {
          this.duplicatePhotos(selectedDomain.id);
        }
      });
    });
  }

  private duplicatePhotos(targetDomainId: string): void {
    let successCount = 0;
    let errorCount = 0;

    Array.from(this.selectedPhotos).forEach((photoId, index) => {
      this.apiService.duplicatePhoto(photoId, targetDomainId).subscribe({
        next: () => {
          successCount++;
        },
        error: (err) => {
          errorCount++;
          if (index === this.selectedPhotos.size - 1) {
            this.notificationService.sendErrorMessage('notifications.photoDuplicatedSomeFailed');
          }
        },
        complete: () => {
          if (index === this.selectedPhotos.size - 1 && errorCount === 0) {
            this.notificationService.sendSuccessMessage('notifications.photoDuplicatedSuccess');
          }
        }
      });
    });
  }

  protected deletePhotos(): void {
    let successCount = 0;
    let errorCount = 0;
    const photosToDuplicateCount = this.selectedPhotos.size;

    Array.from(this.selectedPhotos).forEach((photoId, index) => {
      this.apiService.deletePhoto(photoId).subscribe({
        next: () => {
          successCount++;
        },
        error: (err) => {
          errorCount++;
          console.error(`Error deleting photo ${photoId}:`, err);
          if (index === photosToDuplicateCount - 1) {
            this.notificationService.sendErrorMessage('notifications.photoDeletedSomeFailed');
            this.fetchPhotos(this.currentTag!);
          }
        },
        complete: () => {
          if (index === photosToDuplicateCount - 1 && errorCount === 0) {
            this.notificationService.sendSuccessMessage('notifications.photoDeletedSuccess');
            this.fetchPhotos(this.currentTag!);
          }
        }
      });
    });
    this.selectedPhotos.clear();
  }

  private fetchRootDomainId(): void {
    this.apiService.getRootDomain().subscribe((domain) => {
      this.rootDomainId = domain.id;
      console.log('Root domain id:', this.rootDomainId);
    });
  }


}
