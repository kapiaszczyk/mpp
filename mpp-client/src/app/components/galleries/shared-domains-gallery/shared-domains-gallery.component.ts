import { Component, OnInit, HostListener, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatMenuModule } from '@angular/material/menu';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule } from '@angular/material/dialog';
import { ApiService } from '../../../services/api/api.service';
import { PhotoViewerComponent } from '../../photo-viewer/photo-viewer.component';
import { IndexedDbService } from '../../../services/indexeddb/indexed-db.service';
import { MatDialog } from '@angular/material/dialog';
import { DuplicatePhotoDialogComponent } from '../../dialogs/duplicate-photo-dialog/duplicate-photo-dialog.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BreadcrumbsComponent } from '../../breadcrumbs/breadcrumbs.component';
import { SharedMenuComponent } from '../../shared-menu/shared-menu.component';
import { UploadButtonComponent } from '../../buttons/upload-button/upload-button.component';
import { AlbumCardsComponent } from '../../album-cards/album-cards.component';
import { PhotoCardsComponent } from '../../photo-cards/photo-cards.component';
import { PhotoMetadataWithBlobUrls } from '../../../models/photo-metadata-with-urls.model';
import { Domain } from '../../../models/domain.model';
import { SharedDomainData } from '../../../models/shared-domain-data.model';
import { ShareAlbumButtonComponent } from "../../buttons/share-album-button/share-album-button.component";
import { ShowAlbumInfoButtonComponent } from "../../buttons/show-album-info-button/show-album-info-button.component";
import { PhotoSelectionBarComponent } from '../../photo-selection-bar/photo-selection-bar.component';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { Observable, startWith, map } from 'rxjs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EmptyAlbumCardComponent } from '../../empty-album-card/empty-album-card.component';
import { NoAlbumsCardComponent } from '../../no-albums-card/no-albums-card.component';

@Component({
  selector: 'app-shared-domains-gallery',
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
    MatTooltipModule,
    BreadcrumbsComponent,
    SharedMenuComponent,
    UploadButtonComponent,
    AlbumCardsComponent,
    PhotoCardsComponent,
    ShareAlbumButtonComponent,
    ShowAlbumInfoButtonComponent,
    PhotoSelectionBarComponent,
    MatFormFieldModule,
    MatAutocompleteModule,
    FormsModule,
    MatInputModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
    EmptyAlbumCardComponent,
    NoAlbumsCardComponent
  ],
  templateUrl: './shared-domains-gallery.component.html',
  styleUrl: './shared-domains-gallery.component.scss'
})
export class SharedDomainsGalleryComponent implements OnInit {
  currentSharedDomain: SharedDomainData | null = null;
  currentSharedDomainId: string | null = null;
  sharedDomains: SharedDomainData[] = [];
  sharedDomainsMapped: Domain[] = [];
  breadcrumbs: any = [];
  isPhotoViewerOpen = false;
  photos: PhotoMetadataWithBlobUrls[] = [];
  selectedPhotoIndex = 0;
  selectedPhotos = new Set<string>();
  hoveredPhoto: string | null = null;
  permissionForCurrentDomain: string | null = null;
  menuItems: any;
  tags: any[] = [];
  filteredTags!: Observable<string[]>;
  searchControl = new FormControl('');
  arePhotosLoading = false;
  areNewPhotosBeingUploaded = false;

  uploadEvent!: Event;

  route = inject(ActivatedRoute);
  router = inject(Router);
  indexedDbService = inject(IndexedDbService);
  dialog = inject(MatDialog);
  translate = inject(TranslateService);
  apiService = inject(ApiService);


  // On init, load all domains shared with the user
  ngOnInit(): void {
    this.apiService.getSharedDomains().subscribe((data) => {
      this.sharedDomains = data;
      this.sharedDomainsMapped = this.mapAllSharedDomainToDomain(data);

      // If the domainId in the URL is empty or not in the list of shared domains, navigate to the general shared domains page
      if (!this.sharedDomains.some((domain: { id: string; }) => domain.id === this.route.snapshot.params['domainId'])) {
        this.router.navigate(['/shared']);
      }
      else {
        this.fetchDomainData(this.route.snapshot.params['domainId']);
      }
    });
    this.currentSharedDomainId = this.route.snapshot.params['domainId'];
    this.resolveBreadcrumbs();
    this.loadTags();
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

  onUploadComplete(): void {
    this.fetchPhotos(this.currentSharedDomainId!);
    this.areNewPhotosBeingUploaded = false;
  }

  onUploadStarted(): void {
    this.areNewPhotosBeingUploaded = true;
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
            name: photo.filename,
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
          name: photo.filename,
          uploadDate: photo.uploadDate,
          tags: photo.tags,
          size: thumbnailBlob.size
        };
      });

      this.photos = await Promise.all(photoPromises);

      this.arePhotosLoading = false;
    } catch (error) {
      console.error('Error fetching thumbnails:', error);
    }
  }


  navigateToDomain(domainId: string) {
    // TODO - Fix this
    // this.selectedPhotos.clear();
    if (!domainId) {
      this.router.navigate(['/shared']);
      return;
    }
    this.router.navigate(['/shared/' + domainId]);
  }

  navigateToRootShared() {
    // TODO - Fix this
    // this.selectedPhotos.clear();
    this.router.navigate(['/shared']);
  }

  fetchDomainData(currentDomainId: string | null) {
    if (currentDomainId) {
      this.apiService.getSharedDomainData(currentDomainId).subscribe({
        next: (data) => {
          this.currentSharedDomain = data;
          this.resolveBreadcrumbs(this.currentSharedDomain.name);
          this.fetchPhotos(currentDomainId);
          this.permissionForCurrentDomain = this.currentSharedDomain.roleOfCurrentUser;
        },
        error: (err) => {
          console.error(err);
        },
      });
    }
  }

  openPhotoViewer(index: number) {
    this.selectedPhotoIndex = index;
    this.isPhotoViewerOpen = true;
  }

  closePhotoViewer() {
    this.isPhotoViewerOpen = false;
  }

  async viewPhoto(photoId: string) {
    const selectedPhoto = this.findPhotoById(photoId);
    if (!selectedPhoto.fullUrl) {
      const fullPhotoBlob = await this.apiService.downloadPhoto(selectedPhoto.id.toString()).toPromise();
      const db = await this.indexedDbService.getDb();
      await db.put('photos', { id: selectedPhoto.id, data: fullPhotoBlob });
      selectedPhoto.fullUrl = URL.createObjectURL(fullPhotoBlob);
    }
    this.selectedPhotoIndex = this.photos.findIndex((photo: { id: string; }) => photo.id === photoId);
    this.isPhotoViewerOpen = true;
  }

  togglePhotoSelection(photoId: string, isSelected: boolean): void {
    if (isSelected) {
      this.selectedPhotos.add(photoId);
    } else {
      this.selectedPhotos.delete(photoId);
    }
  }

  // Since for now, the domains in the shared view are displayed in a flat manner
  // The breadcrumbs will instead contain link to the root shared view
  resolveBreadcrumbs(domainName?: string) {
    // If user is viewing a shared domain, the breadcrumbs will contain a link to the root shared view and the domain name
    if (this.currentSharedDomain) {
      this.breadcrumbs = [
        { name: this.translate.instant('sharedAlbums.rootBreadcrumb'), link: '/shared' },
        { name: this.currentSharedDomain.name, link: '/shared/' + domainName },
      ];
    }
  }

  canUploadPhotos(): boolean {
    return this.permissionForCurrentDomain === 'EDITOR' || this.permissionForCurrentDomain === 'ADMINISTRATOR';
  }

  canDeletePhotos(): boolean {
    return this.permissionForCurrentDomain === 'EDITOR' || this.permissionForCurrentDomain === 'ADMINISTRATOR';
  }

  canShareDomain(): boolean {
    return this.permissionForCurrentDomain === 'ADMINISTRATOR';
  }

  openDuplicateDialog(): void {
    this.apiService.getDomains().subscribe((domains) => {
      const dialogRef = this.dialog.open(DuplicatePhotoDialogComponent, {
        width: '400px',
        data: {
          domains: domains,
          currentDomainId: this.currentSharedDomainId,
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

  duplicatePhotos(targetDomainId: string | null): void {
    if (!targetDomainId) return;
    this.selectedPhotos.forEach((photoId) => {
      this.apiService.duplicatePhoto(photoId, targetDomainId).subscribe({
        next: () => {
          this.fetchPhotos(this.currentSharedDomainId!);
        },
        error: (err) => {
          console.error(`Error duplicating photo ${photoId} to domain ${targetDomainId}:`, err);
        },
      });
    });

    this.selectedPhotos.clear();

  }

  downloadPhotos(): void {
    this.selectedPhotos.forEach((photoId) => {
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

    this.selectedPhotos.clear();
  }

  selectedPhotosCount(): number {
    return this.selectedPhotos.size;
  }

  canAccess(role: string): boolean {
    return this.apiService.canAccess(role);
  }

  @HostListener('document:keydown.enter', ['$event'])
  handleEnter(event: KeyboardEvent) {
    if (this.selectedPhotos.size === 0 && this.photos.length > 0 && !this.isPhotoViewerOpen) {
      this.fetchAndCacheFirstPhoto();
      this.openPhotoViewer(this.selectedPhotoIndex);
    }
  }

  private findPhotoById(photoId: string): any {
    return this.photos.find((photo: { id: string; }) => photo.id === photoId);
  }


  // TODO: This is a terrible hack to fetch the first photo in the list and cache it
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


  private mapSharedDomainDataToDomain(data: any): Domain {
    return {
      id: data.id,
      name: data.name,
      parentId: "",
      ownerId: data.ownerId,
      createdAt: data.createdAt,
      thumbnailId: data.thumbnailId,
      path: "",
      permissions: new Map(),
      photoCount: 0,
    };
  }

  protected mapAllSharedDomainToDomain(domains: SharedDomainData[]): Domain[] {
    return domains.map((domain) => this.mapSharedDomainDataToDomain(domain));
  }

  private areAllPhotosSelected(): boolean {
    return this.selectedPhotos.size === this.photos.length;
  }

  isAtLeastOnePhotoSelected(): boolean {
    return this.selectedPhotos.size > 0;
  }

  toggleSelectAll(allSelected: boolean): void {
    // Deslect all photos if all are selected
    if (this.areAllPhotosSelected()) {
      this.selectedPhotos.clear();
    } else {
      // Select all photos if not all are selected
      this.photos.forEach((photo) => {
        this.selectedPhotos.add(photo.id);
      });
    }
  }


}
