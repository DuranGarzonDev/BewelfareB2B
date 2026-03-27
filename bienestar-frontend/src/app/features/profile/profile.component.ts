import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  isMobileMenuOpen: boolean = false;
  role: string | null = '';
  userName: string | null = '';

  // Mensajes flotantes
  showToast = false;
  toastMessage = '';
  isError = false;

  // Modelo de datos del perfil
  profileData = {
    fullName: '',
    email: '',
    bio: '',
    profilePictureUrl: ''
  };

  constructor(private router: Router) {}

  ngOnInit() {
    this.role = localStorage.getItem('role');
    this.userName = localStorage.getItem('fullName');

    if (!localStorage.getItem('token')) {
      this.router.navigate(['/login']);
      return;
    }

    // Cargamos los datos básicos que ya tenemos en el navegador
    this.profileData.fullName = this.userName || '';
    // Nota: El email normalmente lo traeríamos del backend con un GET /api/users/me
    this.profileData.email = 'tu_correo@ufpso.edu.co'; 
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  saveProfile() {
    // Aquí es donde conectaremos con el servicio de Spring Boot más adelante.
    // Ej: this.userService.updateProfile(this.profileData).subscribe(...)
    
    // Por ahora, simulamos el guardado exitoso visualmente:
    if (this.profileData.fullName) {
      localStorage.setItem('fullName', this.profileData.fullName);
      this.userName = this.profileData.fullName;
    }

    this.showNotification('¡Perfil actualizado con éxito!', false);
  }

  showNotification(message: string, isError: boolean) {
    this.toastMessage = message;
    this.isError = isError;
    this.showToast = true;
    setTimeout(() => { this.showToast = false; }, 3000);
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}