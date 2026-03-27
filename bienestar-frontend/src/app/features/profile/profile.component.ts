import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../core/services/user.service'; // 👇 Importamos el nuevo servicio

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
  myUserId: string = '';

  // Mensajes flotantes y estados
  showToast = false;
  toastMessage = '';
  isError = false;
  isLoading = true; // Para saber si estamos trayendo datos
  isSaving = false; // Para que el botón no se presione dos veces

  // Modelo de datos del perfil
  profileData = {
    fullName: '',
    email: '',
    bio: '',
    profilePictureUrl: ''
  };

  constructor(private router: Router, private userService: UserService) {} // 👇 Inyectamos el servicio

  ngOnInit() {
    this.role = localStorage.getItem('role');
    this.myUserId = localStorage.getItem('userId') || '';
    
    if (!localStorage.getItem('token') || !this.myUserId) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadProfile();
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  // 👇 FUNCIÓN PARA CARGAR DATOS DESDE POSTGRESQL
  loadProfile() {
    this.userService.getProfile(this.myUserId).subscribe({
      next: (data) => {
        // Llenamos el formulario con lo que venga de la base de datos
        this.profileData = {
          fullName: data.fullName || '',
          email: data.email || '',
          bio: data.bio || '',
          profilePictureUrl: data.profilePictureUrl || ''
        };
        // Actualizamos el nombre en la interfaz por si acaso
        this.userName = data.fullName;
        this.isLoading = false;
      },
      error: (err) => {
        console.error("Error cargando perfil", err);
        this.showNotification('Error al cargar tu perfil', true);
        this.isLoading = false;
      }
    });
  }

  // 👇 FUNCIÓN PARA GUARDAR LOS CAMBIOS
  saveProfile() {
    if (!this.profileData.fullName || !this.profileData.email) {
      this.showNotification('Nombre y Correo son obligatorios', true);
      return;
    }

    this.isSaving = true;

    this.userService.updateProfile(this.myUserId, this.profileData).subscribe({
      next: (updatedUser) => {
        // Si todo sale bien, actualizamos el localStorage para que el resto de la app sepa tu nuevo nombre
        localStorage.setItem('fullName', updatedUser.fullName);
        this.userName = updatedUser.fullName;
        
        this.showNotification('¡Perfil actualizado con éxito!', false);
        this.isSaving = false;
      },
      error: (err) => {
        console.error("Error guardando perfil", err);
        this.showNotification('Hubo un error al guardar', true);
        this.isSaving = false;
      }
    });
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