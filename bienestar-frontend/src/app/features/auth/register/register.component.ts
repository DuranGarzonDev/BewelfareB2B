import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule], 
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  userData = { fullName: '', email: '', password: '' };
  confirmPassword = '';
  errorMessage = '';

  // Variables UX
  showPassword = false;
  showConfirmPassword = false;
  showToast = false;
  toastMessage = '';
  isLoading = false;

  constructor(private authService: AuthService, private router: Router) {}

  togglePassword() { this.showPassword = !this.showPassword; }
  toggleConfirmPassword() { this.showConfirmPassword = !this.showConfirmPassword; }

  // Computada para saber si las contraseñas coinciden
  get passwordsMatch(): boolean {
    return this.userData.password === this.confirmPassword && this.userData.password.length > 0;
  }

  onSubmit() {
    this.isLoading = true;
    this.errorMessage = '';
    this.authService.register(this.userData).subscribe({
      next: (response: any) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('fullName', response.fullName);
        localStorage.setItem('role', response.role);
        localStorage.setItem('userId', response.userId);

        this.toastMessage = `¡Registro exitoso! Bienvenido, ${response.fullName}`;
        this.showToast = true;

        setTimeout(() => {
          this.showToast = false;
          this.router.navigate(['/dashboard']);
        });
      },
      error: (err) => {
        this.isLoading = false;
        if (err.error && typeof err.error === 'string') {
          this.errorMessage = err.error; // Si el backend manda "El correo ya existe"
        } else {
          this.errorMessage = 'Hubo un error al crear la cuenta. Intenta de nuevo.';
        }
      }
    });
  }
}