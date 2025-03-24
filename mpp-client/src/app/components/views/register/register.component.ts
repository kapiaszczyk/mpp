import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormControl, FormGroup, Validators, ValidationErrors } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../../services/api/api.service';
import { CommonModule } from '@angular/common';
import { AbstractControl } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';
import { NotificationService } from '../../../services/notification/notification.service';
import { LanguageSelectorComponent } from '../../language-selector/language-selector.component';

@Component({
    selector: 'app-register',
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule,
        MatCardModule,
        MatIconModule,
        TranslateModule,
        LanguageSelectorComponent
    ],
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  error: string | null = null;

  apiService = inject(ApiService);
  router = inject(Router);
  notificationService = inject(NotificationService);

  registerForm = new FormGroup({
    username: new FormControl('', [Validators.required]),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(6)]),
    repeatPassword: new FormControl('', [Validators.required, Validators.minLength(6)]),
  }, { validators: this.passwordMatchValidator });

  submit() {
    if (this.registerForm.invalid) {
      return;
    }

    const username = this.registerForm.get('username')?.value ?? '';
    const email = this.registerForm.get('email')?.value ?? '';
    const password = this.registerForm.get('password')?.value ?? '';

    this.apiService.register(email, username, password).subscribe({
      next: () => {
        this.notificationService.sendSuccessMessage('notifications.registerSuccess');
        this.router.navigate(['/login']);
      },
      error: (error) => {
        this.error = error.error;
        this.registerForm.setErrors({ registrationFailed: true });
      }
    });
  }

  passwordMatchValidator(control: AbstractControl) {
    const password = control.get('password');
    const repeatPassword = control.get('repeatPassword');
  
    if (password && repeatPassword && password.value !== repeatPassword.value) {
      repeatPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    } else {
      repeatPassword?.setErrors(null);
      return null;
    }
  }
  

  goToLogin() {
    this.router.navigate(['/login']);
  }
}