import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router'; 
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
      next: (response: any) => {
        // 👇 AQUÍ GUARDAMOS TODO DINÁMICAMENTE EN EL NAVEGADOR
        localStorage.setItem('token', response.token);
        localStorage.setItem('fullName', response.fullName);
        localStorage.setItem('role', response.role);
        localStorage.setItem('userId', response.userId); // ¡El UUID atrapado!

        // Mostramos el Toast elegante
        this.toastMessage = `¡Bienvenido, ${response.fullName}!`;
        this.showToast = true;

        // Esperamos 800ms y lo mandamos al Dashboard
        setTimeout(() => {
          this.showToast = false;
          this.router.navigate(['/dashboard']); 
        }, 800);
      },
      error: (err) => {
        this.errorMessage = 'Credenciales incorrectas o problema de conexión.';
      }
    });
  }
}