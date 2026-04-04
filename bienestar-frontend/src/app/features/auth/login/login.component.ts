import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router'; 
import { AuthService } from '../../../core/services/auth.service';

// 👇 Le decimos a Angular que confíe en el script de Google que pusimos en index.html
declare var google: any;

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
  credentials = { email: '', password: '' };
  errorMessage = '';
  
  // Variables UX
  showPassword = false;
  showToast = false;
  toastMessage = '';

  constructor(
    private authService: AuthService, 
    private router: Router,
    private cdr: ChangeDetectorRef // <-- Para actualizar la vista cuando Google responda
  ) {}

  ngOnInit() {
    // Si ya hay token, lo mandamos al dashboard
    if (localStorage.getItem('token')) {
      this.router.navigate(['/dashboard']);
      return;
    }

    // 👇 INICIALIZAR GOOGLE AL CARGAR LA VISTA
    setTimeout(() => {
      this.initGoogleLogin();
    }, 100); // Pequeño delay para asegurar que el HTML renderizó el div del botón
  }

  // === LÓGICA DE GOOGLE ===
  initGoogleLogin() {
    if (typeof google === 'undefined') {
      console.error("El script de Google no ha cargado. Revisa tu index.html");
      return;
    }

    google.accounts.id.initialize({
      client_id: '52263481218-msc46sqcf93ihgj0s4k8500l1887ppm1.apps.googleusercontent.com', // <--- ¡PEGA TU CLIENT ID AQUÍ!
      callback: this.handleGoogleResponse.bind(this)
    });

    // Dibujamos el botón de Google en el contenedor del HTML
    google.accounts.id.renderButton(
      document.getElementById("google-btn"),
      { theme: "outline", size: "large", width: 50, shape: 'pill' } // Cambiado a número (350)
    );
  }

  handleGoogleResponse(response: any) {
    if (response.credential) {
      this.toastMessage = "Google conectado. Entrando a la plataforma...";
      this.showToast = true;
      this.cdr.detectChanges();

      // 👇 AHORA SÍ, ENVIAMOS EL TOKEN A SPRING BOOT
      this.authService.loginWithGoogle(response.credential).subscribe({
        next: (res: any) => {
          // Guardamos las credenciales generadas por nuestro propio backend
          localStorage.setItem('token', res.token);
          localStorage.setItem('fullName', res.fullName);
          localStorage.setItem('role', res.role);
          localStorage.setItem('userId', res.userId); 

          this.toastMessage = `¡Bienvenido, ${res.fullName}!`;
          this.cdr.detectChanges();

          setTimeout(() => {
            this.showToast = false;
            this.router.navigate(['/dashboard']); 
          }, 800);
        },
        error: (err) => {
          console.error(err);
          this.errorMessage = 'Error al autenticar con Google. Intenta de nuevo.';
          this.showToast = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  // ==========================
  // LÓGICA DE LOGIN MANUAL
  // ==========================

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    this.authService.login(this.credentials).subscribe({
      next: (response: any) => {
        // AQUÍ GUARDAMOS TODO DINÁMICAMENTE EN EL NAVEGADOR
        localStorage.setItem('token', response.token);
        localStorage.setItem('fullName', response.fullName);
        localStorage.setItem('role', response.role);
        localStorage.setItem('userId', response.userId); 

        this.toastMessage = `¡Bienvenido, ${response.fullName}!`;
        this.showToast = true;

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