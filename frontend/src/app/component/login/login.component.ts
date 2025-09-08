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
      this.authService.login(payload).subscribe({
        next: (response) => {
          this.authService.saveSession((response as any).token, (response as any).username);
          this.router.navigate(['/dashboard']);
        },
        error: (error) => console.error('Login failed', error)
      });
    }
  }

  onRegister() {
    if (this.registerForm.valid) {
      const payload = {
        username: this.registerForm.get('username')?.value ?? '',
        email: this.registerForm.get('email')?.value ?? '',
        password: this.registerForm.get('password')?.value ?? ''
      };
      this.authService.register(payload).subscribe({
        next: (response) => {
          this.authService.saveSession((response as any).token, (response as any).username);
          this.router.navigate(['/dashboard']);
        },
        error: (error) => console.error('Registration failed', error)
      });
    }
  }
}