import { Injectable } from '@angular/core';
import { Domain } from '../../models/domain.model';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SharedDomainData } from '../../models/shared-domain-data.model';

@Injectable({
  providedIn: 'root'
})
export class AlbumService {

  getSharedDomainData(albumId: string): Observable<SharedDomainData> {
    if (!albumId) {
      return new Observable((observer) => {
        observer.error('Domain ID is required');
      });
    }
    return this.http.get<SharedDomainData>(`${this.baseUrl}/albums/shared/${albumId}`, {});
  }

  constructor(private http: HttpClient) { }

  private baseUrl = environment.apiUrl;

  getRootDomain() {
    return this.http.get<Domain>(`${this.baseUrl}/albums/root`, {});
  }

  /** Get names and ids of all children domains */
  getChildrenDomains(albumId: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/albums/${albumId}/children`, {});
  }

  /** Get metadata of the current album */
  getDomainData(albumId: string): Observable<Domain> {
    if (!albumId) {
      return new Observable((observer) => {
        observer.error('Domain ID is required');
      });
    }
    return this.http.get<Domain>(`${this.baseUrl}/albums/${albumId}`, {});
  }

  getIdsAndNamesOfDomainsInPath(albumId: string): Observable<any> {
    if (!albumId) {
      return new Observable((observer) => {
        observer.error('Domain ID is required');
      });
    }
    return this.http.get(`${this.baseUrl}/albums/path/${albumId}`, {});
  }

  getSharedUsers(albumId: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/albums/${albumId}/shared-users`, {});
  }

  getPhotosMetadata(albumId: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/photos/album/${albumId}`, {});
  }

  getPhotoFile(photoId: string) {
    return {};
  }

  downloadPhoto(photoId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/photos/download/${photoId}`, {
      responseType: 'blob',
    });
  }

  downloadPhotoThumbnail(photoId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/photos/download/${photoId}/thumbnail`, {
      responseType: 'blob',
    });
  }


  uploadPhoto(albumId: string, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('targetAlbumId', albumId);
    return this.http.post(`${this.baseUrl}/photos/upload`, formData, {
      responseType: 'text',
    });
  }

  createDomain(parentDomainId: string, name: string): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/albums`, {
      "parentAlbumId": parentDomainId,
      "albumName": name
    }, { responseType: 'text' as 'json' });
  }

  deleteDomain(albumId: string, moveChildrenDataToParent: boolean, moveToParentDomain: boolean): Observable<string> {
    return this.http.delete<string>(`${this.baseUrl}/albums/${albumId}`, {
      body: {
        "moveChildrenAlbumsToParentAlbum": moveChildrenDataToParent,
        "movePhotosToParentAlbum": moveToParentDomain
      },
      responseType: 'text' as 'json'
    });
  }

  addSharedUser(albumId: string, userId: string, permission: string): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/albums/${albumId}/permissions`, {
      "userId": userId,
      "role": permission.toUpperCase()
    }, { responseType: 'text' as 'json' });
  }

  updateUserPermission(albumId: string, userId: string, permission: string): Observable<string> {
    return this.http.put<string>(`${this.baseUrl}/albums/${albumId}/permissions`, {
      "userId": userId,
      "role": permission.toUpperCase()
    }, { responseType: 'text' as 'json' });
  }

  removeUserPermission(albumId: string, userId: string): Observable<string> {
    return this.http.delete<string>(`${this.baseUrl}/albums/${albumId}/permissions/${userId}`);
  }

  getSharedDomains(): Observable<any> {
    return this.http.get(`${this.baseUrl}/albums/shared`, {});
  }

  getDomainInfo(albumId: string): Observable<any> {
    return this.http.get<Domain>(`${this.baseUrl}/albums/${albumId}/info`, {});
  }

}
