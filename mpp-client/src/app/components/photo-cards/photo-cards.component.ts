import { AfterViewInit, Component, ElementRef, EventEmitter, inject, Input, Output, QueryList, ViewChildren } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { ApiService } from '../../services/api/api.service';
import { MatDialog } from '@angular/material/dialog';
import { NotificationService } from '../../services/notification/notification.service';
import { MovePhotosDialogComponent } from '../dialogs/move-photos-dialog/move-photos-dialog.component';
import { PhotoMetadataWithBlobUrls } from '../..//models/photo-metadata-with-urls.model';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-photo-cards',
  imports: [
    MatCardModule,
    MatCheckboxModule,
    NgIf,
    NgFor,
    MatProgressSpinnerModule
  ],
  templateUrl: './photo-cards.component.html',
  styleUrl: './photo-cards.component.scss'
})
export class PhotoCardsComponent implements AfterViewInit {

  apiService = inject(ApiService);
  dialog = inject(MatDialog);
  notificationService = inject(NotificationService);

  @Input() photos: PhotoMetadataWithBlobUrls[] = [];
  @Input() selectedPhotos: Set<string> = new Set();
  @Input() currentDomainId: string = '';
  @Input() areNewPhotosLoading: boolean = false;
  @Input() areNewPhotosBeingUploaded: boolean = false;

  @Output() photoClicked = new EventEmitter<string>();
  @Output() photoSelectionChanged = new EventEmitter<{ photoId: string, checked: boolean }>();
  @Output() fetchPhotos = new EventEmitter<string>();
  @Output() getChildrenDomains = new EventEmitter<string>();

  hoveredPhoto: string | null = null;

  @ViewChildren('photoCard', { read: ElementRef }) photoCards!: QueryList<ElementRef>;
  private intersectionObserver?: IntersectionObserver;

  ngAfterViewInit(): void {
    this.photoCards.changes.subscribe(() => {
      this.observeLastPhoto();
    });
    this.observeLastPhoto();
  }

  private observeLastPhoto(): void {
    if (this.intersectionObserver) {
      this.intersectionObserver.disconnect();
    }
  
    const lastCard = this.photoCards.last;
    if (!lastCard) return;
  
    this.intersectionObserver = new IntersectionObserver(entries => {
      const [entry] = entries;
      if (entry.isIntersecting) {
        this.intersectionObserver?.unobserve(entry.target);
        this.fetchPhotos.emit(this.currentDomainId);
      }
    }, {
      rootMargin: '200px',
      threshold: 0.1
    });
  
    this.intersectionObserver.observe(lastCard.nativeElement);
  }
  

  ngOnChanges(): void {
    if (this.areNewPhotosLoading) {
      this.renderBlankLoadingCards();
    }
  }

  selectAllPhotos(select: boolean) {
    if (select) {
      this.photos.forEach(photo => this.selectedPhotos.add(photo.id));
    } else {
      this.selectedPhotos.clear();
    }
  }

  viewPhoto(photoId: string) {
    this.photoClicked.emit(photoId);
  }

  handleCardClick(photoId: string) {
    if (this.selectedPhotos.size > 0) {
      // If at least one photo is selected, toggle selection
      this.togglePhotoSelection(photoId);
    } else {
      // If no photo is selected, open the photo viewer
      this.photoClicked.emit(photoId);
    }
  }

  togglePhotoSelection(photoId: string, checked?: boolean, fromCheckbox: boolean = false) {
    const isCurrentlySelected = this.selectedPhotos.has(photoId);
    // If clicking on the card, toggle selection only if at least one photo is already selected
    if (checked === undefined) {
      checked = !isCurrentlySelected;
    }
    if (checked) {
      this.selectedPhotos.add(photoId);
    } else {
      this.selectedPhotos.delete(photoId);
    }
    // Emit event only if the selection was changed
    if (fromCheckbox) {
      this.photoSelectionChanged.emit({ photoId, checked });
    }
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

  renderBlankLoadingCards(): void {
    console.log('Rendering blank loading cards');
  }

  private movePhotos(photoIds: string[], targetDomainId: string): void {
    let successCount = 0;
    let errorCount = 0;

    photoIds.forEach((photoId, index) => {
      this.apiService.movePhoto(photoId, targetDomainId).subscribe({
        next: () => {
          successCount++;
          if (index === photoIds.length - 1) {
            this.fetchPhotos.emit();
            this.getChildrenDomains.emit();
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
  }

}
