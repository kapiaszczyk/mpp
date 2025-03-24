import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { AlbumService } from '../album/album.service';
import { AuthService } from '../auth/auth.service';
import { catchError, debounceTime, distinctUntilChanged, Observable, of } from 'rxjs';
import { Domain } from '../../models/domain.model';
import { SharedDomainData } from '../../models/shared-domain-data.model';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  
  private baseUrl = environment.apiUrl;
  isReady: Observable<boolean>;
  
  constructor(private http: HttpClient, private photoDomainService: AlbumService, private authService: AuthService) {
    this.isReady = new Observable((observer) => {
      observer.next(true);
    });
  }
  
  /** AUTH ENDPOINT OPERATIONS */
  
  login(email: string, password: string) {
    return this.http.post(`${this.baseUrl}/auth/login`, { email, password });
  }
  
  refresh(refreshToken: string) {
    return this.http.post(`${this.baseUrl}/auth/refresh-token`, { refreshToken });
  }
  
  logout(): Observable<any> {
    this.authService.logout().subscribe({
      next: () => {
        this.authService.removeAccessToken();
        this.authService.removeRefreshToken();
        return new Observable((observer) => {
          observer.next();
        });
      },
      error: (err) => {
        console.error('Logout failed', err);
        return new Observable((observer) => {
          observer.error(err);
        });
      },
      complete: () => {
        return new Observable((observer) => {
          observer.complete();
        });
      },
    });
    return new Observable((observer) => {
      observer.next();
    });
  }
  
  register(email: string, username: string, password: string) {
    return this.http.post(`${this.baseUrl}/auth/register`, { email, username, password }, { responseType: 'text' });
  }
  
  /** DOMAIN ENDPOINT OPERATIONS */
  
  getRootDomain(): Observable<Domain> {
    return this.photoDomainService.getRootDomain();
  }
  
  getDomainData(domainId: string): Observable<Domain> {
    return this.photoDomainService.getDomainData(domainId);
  }
  
  getDomainInfo(domainId: string): Observable<any> {
    return this.photoDomainService.getDomainInfo(domainId);
  }
  
  getSharedDomainData(domainId: string): Observable<SharedDomainData> {
    return this.photoDomainService.getSharedDomainData(domainId);
  }
  
  getCompleteDomainData(domainId: string) {
    let domainData = this.photoDomainService.getDomainData(domainId).subscribe({
      next: (data) => {
        return data;
      },
      error: (err) => {
        console.error('Error getting domain data:', err);
        return err;
      },
    });
    return domainData;
  }
  
  getIdsAndNamesOfDomainsInPath(domainId: string): Observable<any> {
    return this.photoDomainService.getIdsAndNamesOfDomainsInPath(domainId);
  }
  
  getChildrenDomains(domainId: string): Observable<any> {
    return this.photoDomainService.getChildrenDomains(domainId);
  }
  
  getPhotosMetadata(domainId: string): Observable<any> {
    return this.photoDomainService.getPhotosMetadata(domainId);
  }
  
  downloadPhoto(photoId: string): Observable<any> {
    return this.photoDomainService.downloadPhoto(photoId);
  }
  
  downloadPhotoThumbnail(photoId: string): Observable<any> {
    return this.photoDomainService.downloadPhotoThumbnail(photoId);
  }
  
  uploadPhoto(domainId: string, file: File): Observable<any> {
    return this.photoDomainService.uploadPhoto(domainId, file);
  }
  
  createDomain(parentDomainId: string, domainName: string): Observable<string> {
    return this.photoDomainService.createDomain(parentDomainId, domainName);
  }
  
  deleteDomain(domainId: string, moveChildrenDataToParent: boolean, moveToParentDomain: boolean): Observable<string> {
    return this.photoDomainService.deleteDomain(domainId, moveChildrenDataToParent, moveToParentDomain);
  }
  
  getSharedUsers(domainId: string) {
    return this.photoDomainService.getSharedUsers(domainId);
  }
  
  addSharedUser(domainId: string, username: string, permission: string) {
    return this.photoDomainService.addSharedUser(domainId, username, permission);
  }
  
  updateUserPermission(domainId: string, username: string, permission: string) {
    return this.photoDomainService.updateUserPermission(domainId, username, permission);
  }
  
  removeUserPermission(domainId: string, username: string): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.baseUrl}/albums/${domainId}/permissions/${username}`, {
      responseType: 'text' as 'json',
      observe: 'response',
    });
  }
  
  getSharedDomains(): Observable<any> {
    return this.photoDomainService.getSharedDomains();
  }
  
  getPhotoDownloadUrl(photoId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/photos/download/${photoId}`, { responseType: 'blob' });
  }
  
  getDomains(): Observable<any> {
    return this.http.get(`${this.baseUrl}/albums/all`);
  }
  
  movePhoto(photoId: string, targetDomainId: string) {
    return this.http.put<void>(`${this.baseUrl}/photos/${photoId}/move/${targetDomainId}`, {});
  }
  
  deletePhoto(photoId: string) {
    return this.http.delete<void>(`${this.baseUrl}/photos/${photoId}`);
  }
  
  duplicatePhoto(photoId: string, targetDomainId: string) {
    return this.http.post<void>(`${this.baseUrl}/photos/${photoId}/duplicate/${targetDomainId}`, {});
  }
  
  getPhotosMetadataForTag(tag: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/photos/tag/${tag}`);
  }
  
  getTags(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/photos/tags`);
  }
  
  getSpaceUsedInSystem(): Observable<any> {
    return this.http.get(`${this.baseUrl}/admin/statistics/space`);
  }
  
  getDomainsInSystem(): Observable<any> {
    return this.http.get(`${this.baseUrl}/admin/statistics/albums`);
  }
  
  getUsersInSystem(): Observable<any> {
    return this.http.get(`${this.baseUrl}/admin/users`);
  }
  
  getRolesInSystem(): Observable<any> {
    return this.http.get(`${this.baseUrl}/admin/roles`);
  }
  
  getNumberOfPhotosInSystem(): Observable<any> {
    return this.http.get(`${this.baseUrl}/admin/statistics/photos`);
  }
  
  getNumberOfUsersInSystem(): Observable<any> {
    return this.http.get(`${this.baseUrl}/admin/statistics/users`);
  }
  
  getNumberOfDomainsInSystem(): Observable<any> {
    return this.http.get(`${this.baseUrl}/admin/statistics/albums`);
  }
  
  getStatisticsForUser(username: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/admin/statistics/${username}`);
  }
  
  canAccess(role: string): boolean {
    return this.authService.canAccess(role.toUpperCase());
  }
  
  changeRole(userId: string, role: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/admin/user/${userId}/role/${role}`, {});
  }
  
  deleteUser(userId: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/admin/delete/user/${userId}`)
  }
  
  updatePhotoTags(photoId: string, tags: string[]): Observable<any> {
    return this.http.put(`${this.baseUrl}/photos/${photoId}/tags`, tags);
  }
  
  getPhotosByTagGroupedByDomain(tag: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/photos/tag/${tag}/grouped`);
  }
  
  setAsAlbumCover(domainId: string, photoId: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/albums/${domainId}/photo/${photoId}/cover`, {});
  }
  
  removeAlbumCover(domainId: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/albums/${domainId}/cover`);
  }
  
  renameAlbum(domainId: string, newName: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/albums/${domainId}/rename`, newName );
  }

  searchUsers(query: string): Observable<any[]> {
    if (!query || query.length < 2) {
      return of([]); 
    }

    return this.http.get<any[]>(`${this.baseUrl}/users/search?query=${query}`).pipe(
      debounceTime(300),
      distinctUntilChanged(), 
      catchError(() => of([]))
    );
  }
}
