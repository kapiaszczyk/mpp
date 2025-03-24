import { Component, inject, Inject, Input } from '@angular/core';
import { Domain } from '../../models/domain.model';
import { MatCardModule } from '@angular/material/card';
import { NgFor, NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatIconModule } from '@angular/material/icon';
import { ApiService } from '../../services/api/api.service';
import { IndexedDbService } from '../../services/indexeddb/indexed-db.service';

@Component({
  selector: 'app-album-cards',
  imports: [
    MatCardModule,
    NgFor,
    NgIf,
    TranslateModule,
    MatTooltipModule,
    MatIconModule
  ],
  templateUrl: './album-cards.component.html',
  styleUrl: './album-cards.component.scss'
})
export class AlbumCardsComponent {

  @Input() albums: Domain[] = [];
  @Input() navigateToDomain!: (id: string) => void;

  router = inject(Router);

  albumThumbnailUrls: { id: string, url: string }[] = [];

  apiService = inject(ApiService);
  indexedDbService = inject(IndexedDbService);
  
  ngOnChanges() {
    this.fetchCoversOfTheAlbums();
  }

  async fetchCoversOfTheAlbums() {
    const db = await this.indexedDbService.getDb();

    try {
      const coverPromises = this.albums.map(async (album: Domain) => {
        try {
          if (!album.thumbnailId) {
            return { id: album.id, url: '' };
          }
      
          const cachedCover = await db.get('thumbnails', album.thumbnailId);
          
          if (cachedCover) {
            return { id: album.id, url: URL.createObjectURL(cachedCover.data) };
          }
      
          const coverBlob = await this.apiService.downloadPhotoThumbnail(album.thumbnailId).toPromise();
          await db.put('thumbnails', { id: album.thumbnailId, data: coverBlob });
      
          return { id: album.id, url: URL.createObjectURL(coverBlob) };
        } catch (error) {
          return { id: album.id, url: '' };
        }
      });
      
      const covers = await Promise.all(coverPromises);
  
      this.albums.forEach((album) => {
        const cover = covers.find((c) => c.id === album.id);
        if (cover) {
          album.thumbnailUrl = cover.url;
        }
      });
  
    } catch (error) {
      console.error('Error fetching covers:', error);}
  }
  

}
