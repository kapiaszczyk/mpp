import { Routes } from '@angular/router';
import { LoginComponent } from './components/views/login/login.component';
import { RegisterComponent } from './components/views/register/register.component';
import { GalleryComponent } from './components/galleries/gallery/gallery.component';
import { authGuard } from './guards/auth.guard';
import { SharedDomainsGalleryComponent } from './components/galleries/shared-domains-gallery/shared-domains-gallery.component';
import { TagGalleryComponent } from './components/galleries/tag-gallery/tag-gallery.component';
import { AdminViewComponent } from './components/views/admin-view/admin-view.component';

export const routes: Routes = [
  // Default route
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  // Login route
  { path: 'login', component: LoginComponent },
  // Register route
  { path: 'register', component: RegisterComponent },
  // Root gallery route
  { path: 'gallery', component: GalleryComponent, canActivate: [authGuard] },
  // Gallery route
  { path: 'gallery/:domainId', component: GalleryComponent, canActivate: [authGuard] },
  // Shared domains route
  { path: 'shared', component: SharedDomainsGalleryComponent, canActivate: [authGuard] },
  // Shared domain route with domain ID
  { path: 'shared/:domainId', component: SharedDomainsGalleryComponent, canActivate: [authGuard] },
  // All tags route
  { path: 'tag-gallery', component: TagGalleryComponent, canActivate: [authGuard] },
  // Photos by tags route
  { path: 'tag-gallery/:tag', component: TagGalleryComponent, canActivate: [authGuard] },
  // Admin panel
  { path: 'admin-panel', component: AdminViewComponent, canActivate: [authGuard], data: { role: 'ADMIN' } }

];
