import { Injectable } from '@angular/core';
import { openDB, IDBPDatabase } from 'idb';

@Injectable({
  providedIn: 'root',
})
export class IndexedDbService {
  private dbPromise: Promise<IDBPDatabase>;

  constructor() {
    this.dbPromise = this.initializeDb();
  }

  private initializeDb(): Promise<IDBPDatabase> {
    return openDB('photo-cache', 1, {
      upgrade(db) {
        if (!db.objectStoreNames.contains('photos')) {
          db.createObjectStore('photos', { keyPath: 'id' });
        }

        if (!db.objectStoreNames.contains('thumbnails')) {
          db.createObjectStore('thumbnails', { keyPath: 'id' });
        }

        if (!db.objectStoreNames.contains('album-thumbnails')) {
          db.createObjectStore('album-thumbnails', { keyPath: 'id' });
        }
      },
    });
  }

  getDb(): Promise<IDBPDatabase> {
    return this.dbPromise;
  }
}