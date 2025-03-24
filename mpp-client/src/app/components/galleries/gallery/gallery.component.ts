import { Component, OnInit, HostListener, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { ApiService } from '../../../services/api/api.service';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Domain, mapToDomain } from '../../../models/domain.model';
import { IndexedDbService } from '../../../services/indexeddb/indexed-db.service';
import { PhotoViewerComponent } from '../../photo-viewer/photo-viewer.component';
import { MatMenuModule } from '@angular/material/menu';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDialog } from '@angular/material/dialog';
import { MovePhotosDialogComponent } from '../../dialogs/move-photos-dialog/move-photos-dialog.component';
import { DuplicatePhotoDialogComponent } from '../../dialogs/duplicate-photo-dialog/duplicate-photo-dialog.component';
import { TranslateModule } from '@ngx-translate/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BreadcrumbsComponent } from '../../breadcrumbs/breadcrumbs.component';
import { SharedMenuComponent } from '../../shared-menu/shared-menu.component';
import { NotificationService } from '../../../services/notification/notification.service';
import { UploadButtonComponent } from '../../buttons/upload-button/upload-button.component';
import { ChangeDetectorRef } from '@angular/core';
import { AlbumCardsComponent } from '../../album-cards/album-cards.component';
import { PhotoCardsComponent } from '../../photo-cards/photo-cards.component';
import { PhotoMetadataWithBlobUrls } from '../../../models/photo-metadata-with-urls.model';
import { AddAlbumButtonComponent } from '../../buttons/add-album-button/add-album-button.component';
import { DeleteAlbumButtonComponent } from "../../buttons/delete-album-button/delete-album-button.component";
import { ShareAlbumButtonComponent } from '../../buttons/share-album-button/share-album-button.component';
import { ShowAlbumInfoButtonComponent } from "../../buttons/show-album-info-button/show-album-info-button.component";
import { RemoveAlbumCoverButtonComponent } from "../../buttons/remove-album-cover-button/remove-album-cover-button.component";
import { PhotoSelectionBarComponent } from '../../photo-selection-bar/photo-selection-bar.component';
import { UtilService } from '../../../services/util/util.service';
import { RenameAlbumButtonComponent } from "../../buttons/rename-album-button/rename-album-button.component";
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { Observable, startWith, map } from 'rxjs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EmptyAlbumCardComponent } from '../../empty-album-card/empty-album-card.component';
import { NoAlbumsCardComponent } from '../../no-albums-card/no-albums-card.component';

// TODO: Fix the sidenav state (and better, make it a separate component)
@Component({
  selector: 'app-gallery',
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatSidenavModule,
    MatIconModule,
    MatToolbarModule,
    PhotoViewerComponent,
    MatMenuModule,
    MatCheckboxModule,
    MatDialogModule,
    TranslateModule,
    MatTooltipModule,
    BreadcrumbsComponent,
    SharedMenuComponent,
    UploadButtonComponent,
    AlbumCardsComponent,
    PhotoCardsComponent,
    AddAlbumButtonComponent,
    DeleteAlbumButtonComponent,
    ShareAlbumButtonComponent,
    ShowAlbumInfoButtonComponent,
    RemoveAlbumCoverButtonComponent,
    PhotoSelectionBarComponent,
    RenameAlbumButtonComponent,
    MatFormFieldModule,
    MatAutocompleteModule,
    FormsModule,
    MatInputModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
    EmptyAlbumCardComponent,
    NoAlbumsCardComponent
  ],
  templateUrl: './gallery.component.html',
  styleUrl: './gallery.component.scss'
})
export class GalleryComponent implements OnInit {
  currentDomainData: Domain | null = null;
  currentDomainId: string | null = null;
  photos: any[] = [];
  domains: Domain[] = [];
  breadcrumbs: any[] = [];
  isPhotoViewerOpen = false;
  selectedPhotoIndex = 0;
  selectedPhotos = new Set<string>();
  hoveredPhoto: string | null = null;
  menuItems: any;
  tags: any[] = [];
  filteredTags!: Observable<string[]>;
  searchControl = new FormControl('');
  arePhotosLoading = false;
  areNewPhotosBeingUploaded = false;

  uploadEvent!: Event;

  route = inject(ActivatedRoute);
  notificationService = inject(NotificationService);
  router = inject(Router);
  apiService = inject(ApiService);
  indexedDbService = inject(IndexedDbService);
  dialog = inject(MatDialog);
  cdr = inject(ChangeDetectorRef);
  utilService = inject(UtilService);

  onUploadStarted(): void {
    this.areNewPhotosBeingUploaded = true;
  }

  onUploadComplete(): void {
    if (this.currentDomainId) {
      this.areNewPhotosBeingUploaded = false;
      this.fetchPhotos(this.currentDomainId);
    }
  }

  ngOnInit() {
    this.route.params.subscribe((params) => {
      this.currentDomainId = params['domainId'];
      if (this.currentDomainId) {
        this.fetchDomainData(this.currentDomainId);
        this.cdr.detectChanges();
      }
    });
    this.apiService.isReady.subscribe(() => {
      this.menuItems = [
        { label: 'mainMenu.mainGallery', route: '/gallery' },
        { label: 'mainMenu.tagView', route: '/tag-gallery' },
        { label: 'mainMenu.sharedAlbums', route: '/shared' },
        { label: 'mainMenu.adminView', route: '/admin-panel', hidden: !this.canAccess('admin') },
        { label: 'mainMenu.logout', route: '/logout' },
      ];
    });
    this.loadTags();
    this.filteredTags = this.searchControl.valueChanges.pipe(
      startWith(''),
      map(value => this.filterTags(value || '').slice(0, 10))
    );
  }

  ngOnDestroy(): void {
    this.clearPhotoUrls();
  }

  private clearSelectedPhotos(): void {
    console.log('Clearing selected photos');
    this.photos.forEach((photo) => photo.isSelected = false);
    this.selectedPhotos.clear();
  }

  loadTags(): void {
    this.apiService.getTags().subscribe((tags: any) => {
      this.tags = tags;
    });
  }


  private filterTags(value: string): string[] {
    const filterValue = value.toLowerCase();
    return this.tags.filter((tag: string) => tag.toLowerCase().includes(filterValue));
  }


  searchByTag(tag: string): void {
    if (tag.trim()) {
      this.router.navigate(['/tag-gallery/', tag]);
    }
  }

  protected fetchDomainData(currentDomainId: string | null) {
    if (currentDomainId) {
      this.apiService.getDomainData(currentDomainId).subscribe({
        next: (data) => {
          this.currentDomainData = data;
          this.resolveBreadcrumbsFromData(currentDomainId, this.currentDomainData.path);
          this.getChildrenDomains(currentDomainId);
          this.fetchPhotos(currentDomainId);
        },
        error: (err) => {
          console.error(err);
        },
      });
    }
  }

  protected navigateToDomain = (domainId: string) => {
    this.clearSelectedPhotos();
    this.router.navigate(['/gallery', domainId]);
  };

  private resolveBreadcrumbsFromData(currentDomainId: string, currentDomainPath: string): void {
    this.apiService.getIdsAndNamesOfDomainsInPath(currentDomainId).subscribe({
      next: (data: any[]) => {
        const pathSegments = currentDomainPath.split('/').filter(segment => segment);
        this.breadcrumbs = pathSegments.map((segment, index) => {
          const matchingDomain = data.find(domain => domain.name === segment);
          return matchingDomain
            ? { id: matchingDomain.id, name: matchingDomain.name }
            : { id: '', name: segment };
        });
      },
      error: (err) => {
        console.error('Error fetching breadcrumbs:', err);
        this.breadcrumbs = [];
      }
    });
  }

  protected getChildrenDomains(domainId: string) {
    this.apiService.getChildrenDomains(domainId).subscribe({
      next: (data) => {
        this.domains = data.map(mapToDomain);
      },
      error: (err) => {
        console.error('Error fetching children domains:', err);
        this.domains = [];
      }
    });
  }

  private async fetchPhotos(domainId: string): Promise<void> {
    this.photos = [];
    const db = await this.indexedDbService.getDb();
    this.arePhotosLoading = true;

    try {
      const metadata = await this.apiService.getPhotosMetadata(domainId).toPromise();

      const photoPromises = metadata.map(async (photo: { id: IDBKeyRange | IDBValidKey; filename: any; uploadDate: any; tags: any; size: any; }) => {

        const cachedThumbnail = await db.get('thumbnails', photo.id);
        if (cachedThumbnail) {
          const objectUrl = URL.createObjectURL(cachedThumbnail.data);
          return {
            id: photo.id,
            url: objectUrl,
            filename: photo.filename,
            uploadDate: photo.uploadDate,
            tags: photo.tags,
            size: photo.size
          };
        }

        const thumbnailBlob = await this.apiService.downloadPhotoThumbnail(photo.id.toString()).toPromise();
        await db.put('thumbnails', { id: photo.id, data: thumbnailBlob });

        const objectUrl = URL.createObjectURL(thumbnailBlob);
        return {
          id: photo.id,
          url: objectUrl,
          filename: photo.filename,
          uploadDate: photo.uploadDate,
          tags: photo.tags,
          size: thumbnailBlob.size
        };
      });

      this.photos = await Promise.all(photoPromises);

      this.arePhotosLoading = false;

      console.log('Done fetching photos');
    } catch (error) {
      console.error('Error fetching thumbnails:', error);
    }
  }

  private clearPhotoUrls(): void {
    this.photos.forEach((photo) => URL.revokeObjectURL(photo.url));
  }

  private openPhotoViewer(index: number) {
    this.fetchAndCacheFirstPhoto();
    this.selectedPhotoIndex = index;
    this.isPhotoViewerOpen = true;
  }

  protected closePhotoViewer() {
    this.isPhotoViewerOpen = false;
  }

  async viewPhoto(photoId: string) {
    const selectedPhoto = this.findPhotoById(photoId)!;
    if (!selectedPhoto.fullUrl) {
      const fullPhotoBlob = await this.apiService.downloadPhoto(selectedPhoto.id.toString()).toPromise();
      const db = await this.indexedDbService.getDb();
      await db.put('photos', { id: selectedPhoto.id, data: fullPhotoBlob });
      selectedPhoto.fullUrl = URL.createObjectURL(fullPhotoBlob);
    }
    this.selectedPhotoIndex = this.photos.findIndex(photo => photo.id === photoId);
    this.isPhotoViewerOpen = true;
  }

  protected openDuplicateDialog(): void {
    this.apiService.getDomains().subscribe((domains) => {
      const dialogRef = this.dialog.open(DuplicatePhotoDialogComponent, {
        data: {
          domains: domains,
          currentDomainId: this.currentDomainId,
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
    const photosToDuplicateCount = this.selectedPhotos.size;

    Array.from(this.selectedPhotos).forEach((photoId, index) => {
      this.apiService.duplicatePhoto(photoId, targetDomainId).subscribe({
        next: () => {
          successCount++;
        },
        error: (err) => {
          errorCount++;
          if (index === photosToDuplicateCount - 1) {
            this.notificationService.sendErrorMessage('notifications.photoDuplicatedSomeFailed');
          }
        },
        complete: () => {
          if (index === photosToDuplicateCount - 1 && errorCount === 0) {
            this.notificationService.sendSuccessMessage('notifications.photoDuplicatedSuccess');
          }
        }
      });
    });
    this.clearSelectedPhotos();
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
            this.fetchPhotos(this.currentDomainId!);
          }
        },
        complete: () => {
          if (index === photosToDuplicateCount - 1 && errorCount === 0) {
            this.notificationService.sendSuccessMessage('notifications.photoDeletedSuccess');
            this.fetchPhotos(this.currentDomainId!);
          }
        }
      });
    });
    this.clearSelectedPhotos();
  }

  protected setAsAlbumCover(): void {
    const photoId = Array.from(this.selectedPhotos)[0];
    this.utilService.setPhotoAsAlbumCover(this.currentDomainId!, photoId, () => {
      this.selectedPhotos.clear();
    }).subscribe();
  }

  downloadPhotos(): void {
    this.selectedPhotos.forEach((photoId) => {
      // Get name from photo metadata
      const photo = this.photos.find((p: { id: string; }) => p.id === photoId);
      if (!photo) {
        console.error(`Photo ${photoId} not found in metadata`);
        return;
      }

      // Use an anchor element to trigger the download
      this.apiService.getPhotoDownloadUrl(photoId).subscribe({
        next: (photoBlob) => {
          const blob = new Blob([photoBlob], { type: photoBlob.type });
          const url = window.URL.createObjectURL(blob);

          const a = document.createElement('a');
          a.href = url;
          a.download = `${photo.name}`;
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
    this.clearSelectedPhotos();
  }

  protected openMoveDialog(): void {
    this.apiService.getDomains().subscribe((domains) => {
      const dialogRef = this.dialog.open(MovePhotosDialogComponent, {
        width: '400px',
        data: {
          domains: domains,
          currentDomainId: this.currentDomainId,
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
          if (index === photoIds.length - 1) {
            this.getChildrenDomains(this.currentDomainId!);
            this.fetchPhotos(this.currentDomainId!);
          }
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
    this.clearSelectedPhotos();
  }

  private canAccess(role: string): boolean {
    return this.apiService.canAccess(role);
  }

  @HostListener('document:keydown.enter', ['$event'])
  private handleEnter(event: KeyboardEvent) {
    if (this.selectedPhotos.size === 0 && this.photos.length > 0 && !this.isPhotoViewerOpen && this.dialog.openDialogs.length === 0) {
      this.selectedPhotoIndex = 0;
      this.openPhotoViewer(this.selectedPhotoIndex);
    }
  }

  protected selectedPhotosCount(): number {
    return this.selectedPhotos.size;
  }

  private findPhotoById(photoId: string): PhotoMetadataWithBlobUrls {
    return this.photos.find((photo) => photo.id === photoId);
  }

  // TODO: This is a hack to fetch the first photo in the list and cache it
  private async fetchAndCacheFirstPhoto() {
    const firstPhoto = this.photos[0];
    if (!firstPhoto) {
      return;
    }

    try {
      const photoBlob = await this.apiService.downloadPhoto(firstPhoto.id).toPromise();
      const db = await this.indexedDbService.getDb();
      const objectUrl = URL.createObjectURL(photoBlob);

      // Save the url in the photo metadata
      this.photos.find((photo) => photo.id === firstPhoto.id)!.fullUrl = objectUrl;

      await db.put('photos', { id: firstPhoto.id, data: photoBlob });
    } catch (error) {
      console.error('Error fetching photo:', error);
    }
  }

  isInRootAlbum(): boolean {
    return this.currentDomainData?.parentId === '';
  }

  isInRootAlbumButPhotosPresent(): boolean {
    return this.isInRootAlbum() && this.photos.length > 0;
  }

  protected togglePhotoSelection(photoId: string, isSelected: boolean): void {
    if (isSelected) {
      this.selectedPhotos.add(photoId);
    } else {
      this.selectedPhotos.delete(photoId);
    }
  }

  private areAllPhotosSelected(): boolean {
    return this.selectedPhotos.size === this.photos.length;
  }

  protected isAtLeastOnePhotoSelected(): boolean {
    return this.selectedPhotos.size > 0;
  }

  toggleSelectAll(allSelected: boolean): void {
    // Deslect all photos if all are selected
    if (this.areAllPhotosSelected()) {
      this.selectedPhotos.clear();
      this.photos.forEach((photo) => {
        photo.isSelected = false;
      });
    } else {
      // Select all photos if not all are selected
      this.photos.forEach((photo) => {
        this.selectedPhotos.add(photo.id);
        photo.isSelected = true;
      });
    }
  }

}
