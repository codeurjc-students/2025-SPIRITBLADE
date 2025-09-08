import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  isLoginMode = true;

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });
  
  loginData = {
    email: '',
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
      this.authService.login(this.loginForm.value).subscribe({
        next: (response) => this.router.navigate(['/dashboard']),
        error: (error) => console.error('Login failed', error)
      });
    }
  }

  onRegister() {
    if (this.registerForm.valid) {
      this.authService.register(this.registerForm.value).subscribe({
        next: (response) => this.router.navigate(['/dashboard']),
        error: (error) => console.error('Registration failed', error)
      });
    }
  }
}