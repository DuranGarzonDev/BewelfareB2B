import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../core/services/user.service';

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
  isLoading = true;
  isSaving = false;

  // Modelo de datos del perfil
  profileData = {
    fullName: '',
    email: '',
    bio: '',
    profilePictureUrl: ''
  };

  constructor(private router: Router, private userService: UserService) {}

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

  loadProfile() {
    this.userService.getProfile(this.myUserId).subscribe({
      next: (data) => {
        this.profileData = {
          fullName: data.fullName || '',
          email: data.email || '',
          bio: data.bio || '',
          profilePictureUrl: data.profilePictureUrl || ''
        };
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

  saveProfile() {
    // Aunque estén bloqueados en la vista, validamos por seguridad
    if (!this.profileData.fullName || !this.profileData.email) {
      this.showNotification('Nombre y Correo son obligatorios', true);
      return;
    }

    this.isSaving = true;

    this.userService.updateProfile(this.myUserId, this.profileData).subscribe({
      next: (updatedUser) => {
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