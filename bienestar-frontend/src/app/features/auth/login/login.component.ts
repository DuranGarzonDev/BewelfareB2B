import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router'; // Añadimos Router
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  credentials = { email: '', password: '' };
  errorMessage = '';
  
  // Variables UX
  showPassword = false;
  showToast = false;
  toastMessage = '';

  constructor(private authService: AuthService, private router: Router) {}

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        // Mostramos el Toast elegante
        this.toastMessage = `¡Bienvenido, ${response.fullName}!`;
        this.showToast = true;

        // Esperamos 1.5 segundos y lo mandamos al Dashboard
        setTimeout(() => {
          this.showToast = false;
          this.router.navigate(['/dashboard']); // <-- Nos llevará a la siguiente fase
        }, 800);
      },
      error: (err) => {
        this.errorMessage = 'Credenciales incorrectas o problema de conexión.';
      }
    });
  }
}