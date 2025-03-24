import { Injectable } from '@angular/core';
import { ApiService } from '../api/api.service';
import { IndexedDbService } from '../indexeddb/indexed-db.service';

@Injectable({
  providedIn: 'root'
})
export class LogoutService {

  constructor(private indexedDbService: IndexedDbService, private apiService: ApiService) { }

  logout() {
    this.apiService.logout().subscribe({
      next: () => {
        this.clearCacheOfPhotos();
        localStorage.clear();
        sessionStorage.clear();
      },
      error: (err) => {
        console.error('Logout failed', err);
      },
      complete: () => {
      }
    });
  }

  async clearCacheOfPhotos(): Promise<void> {
    const db = await this.indexedDbService.getDb();
    const photos = await db.getAll('photos');
    photos.forEach((photo) => URL.revokeObjectURL(photo.url));
    await db.clear('photos');
  }
}
