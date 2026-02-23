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

  constructor(private authService: AuthService, private router: Router) {}

  togglePassword() { this.showPassword = !this.showPassword; }
  toggleConfirmPassword() { this.showConfirmPassword = !this.showConfirmPassword; }

  // Computada para saber si las contraseñas coinciden
  get passwordsMatch(): boolean {
    return this.userData.password === this.confirmPassword && this.userData.password.length > 0;
  }

  onSubmit() {
    this.authService.register(this.userData).subscribe({
      next: (response) => {
        this.toastMessage = `¡Registro exitoso! Bienvenido, ${response.fullName}`;
        this.showToast = true;

        setTimeout(() => {
          this.showToast = false;
          this.router.navigate(['/login']);
        });
      },
      error: (err) => {
        this.errorMessage = 'Hubo un error. Es posible que el correo ya esté en uso.';
      }
    });
  }
}