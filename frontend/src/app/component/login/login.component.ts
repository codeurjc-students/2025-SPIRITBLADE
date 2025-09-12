import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  isLoginMode = true;
  message: { type: 'error' | 'success' | 'info', text: string } | null = null;
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);

  loginForm = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  registerForm = this.fb.group({
    username: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
    confirmPassword: ['', Validators.required]
  });
  
  loginData = {
    username: '',
    password: ''
  };
  
  registerData = {
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  };

  showLogin() {
    this.isLoginMode = true;
  }

  showRegister() {
    this.isLoginMode = false;
  }

  onLogin() {
    if (this.loginForm.valid) {
      const payload = {
        username: this.loginForm.get('username')?.value ?? '',
        password: this.loginForm.get('password')?.value ?? ''
      };
      this.message = null;
      this.authService.login(payload).subscribe({
        next: (response: any) => {
          // Successful login — backend may set cookies or return token
          if (response?.token) {
            this.authService.saveSession(response.token, response.username);
          }
          this.message = { type: 'success', text: 'Login successful. Redirecting...' };
          setTimeout(() => this.router.navigate(['/profile']), 600);
        },
        error: (err: any) => {
          console.error('Login failed', err);
          // Show friendly message depending on status
          if (err?.status === 401 || err?.status === 403) {
            this.message = { type: 'error', text: 'Invalid credentials. Please check your username/password.' };
          } else if (err?.status === 0) {
            this.message = { type: 'error', text: 'Could not connect to the server. Please ensure the backend is running.' };
          } else {
            this.message = { type: 'error', text: 'Unexpected error during login.' };
          }
        }
      });
    }
  }

  onRegister() {
    if (this.registerForm.valid) {
      const password = this.registerForm.get('password')?.value ?? '';
      const confirm = this.registerForm.get('confirmPassword')?.value ?? '';

      if (password !== confirm) {
        console.error('Passwords do not match');
        return;
      }

      const payload = {
        name: this.registerForm.get('username')?.value ?? '',
        email: this.registerForm.get('email')?.value ?? '',
        password
      };

      this.authService.register(payload).subscribe({
        next: () => {
          this.message = { type: 'success', text: 'Registration successful. Logging in...' };
          this.authService.login({ username: payload.name, password: payload.password }).subscribe({
            next: (loginResp: any) => {
              if ((loginResp as any).token) {
                this.authService.saveSession((loginResp as any).token, (loginResp as any).username);
              }
              this.message = { type: 'success', text: 'Automatic login completed. Redirecting...' };
              setTimeout(() => this.router.navigate(['/profile']), 600);
            },
            error: (err: any) => {
              console.error('Auto-login after registration failed', err);
              this.message = { type: 'info', text: 'Registration successful, but automatic login failed. Please try logging in manually.' };
            }
          });
        },
        error: (error: any) => {
          console.error('Registration failed', error);
          if (error?.status === 409) {
            this.message = { type: 'error', text: 'User already exists.' };
          } else if (error?.status === 400) {
            this.message = { type: 'error', text: 'Invalid registration data.' };
          } else {
            this.message = { type: 'error', text: 'Error registering user.' };
          }
        }
      });
    }
  }
}