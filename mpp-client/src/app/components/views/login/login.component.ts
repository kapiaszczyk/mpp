import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../../services/api/api.service';
import { AuthService } from '../../../services/auth/auth.service';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'app-login',
    imports: [
        ReactiveFormsModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule,
        MatCardModule,
        MatIconModule,
        CommonModule,
        TranslateModule,
    ],
    templateUrl: './login.component.html',
    styleUrl: './login.component.scss'
})
export class LoginComponent {

  router = inject(Router);
  apiService = inject(ApiService);
  authService = inject(AuthService);
  translationService = inject(TranslateService);

  error: string | null = null;

  rootDomainId: string = '';

  loginForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required]),
  });

  submit() {
    if (this.loginForm.invalid) {
      return;
    }

    const email = this.loginForm.get('email')?.value ?? '';
    const password = this.loginForm.get('password')?.value ?? '';

    this.apiService.login(email, password).subscribe({
      next: (response: any) => {
        this.authService.saveToken(response.accessToken);
        this.authService.saveRefreshToken(response.refreshToken);
        this.apiService.getRootDomain().subscribe({
          next: (response: any) => {
            this.rootDomainId = response.id;
            this.router.navigate(['/gallery', this.rootDomainId]);
          }
        });
      },
      error: (error) => {
        if (error.status === 401) {
          this.error = this.translationService.instant('notifications.loginError');
        } else if (error.status === 404) {
          this.error = this.translationService.instant('notifications.loginNoAccountMatch');
        }
        else if (error.status === 0) {
          this.error = this.translationService.instant('notifications.connectionToTheServerFailed');
        }
        else {
          this.error = this.translationService.instant('notifications.serverError');
        }
      }
    });

  }

  goToRegister() {
    this.router.navigate(['/register']);
  }
}
