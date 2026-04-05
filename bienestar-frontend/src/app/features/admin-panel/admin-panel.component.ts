import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AdminService } from '../../core/services/admin.service';
import { BreakService } from '../../core/services/break.service';
import { UserService } from '../../core/services/user.service'; // 👇 INYECTADO PARA LA FOTO

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './admin-panel.component.html'
})
export class AdminPanelComponent implements OnInit {
  userName: string | null = '';
  role: string | null = '';
  myUserId: string = '';
  profilePic: string | null = null; // 👇 VARIABLE PARA LA FOTO
  
  showToast = false;
  toastMessage = '';
  isError = false;

  // 📊 Métricas B2B
  companyName: string = 'Cargando datos...';
  totalEmployees: number = 0;
  totalBreaks: number = 0;
  totalCoins: number = 0;
  isLoadingStats = true;

  // 📝 Variables de Formularios
  categories: any[] = [];
  newCategory = { name: '', description: '' };
  newBreak = { title: '', description: '', durationSeconds: 60, mediaUrl: '', coinReward: 10, categoryId: null as number | null };
  newInvite = { email: '' }; // Para la futura invitación

  isMobileMenuOpen: boolean = false;

  // 🪟 CONTROLADORES DE MODALES
  isCategoryModalOpen = false;
  isBreakModalOpen = false;
  isInviteModalOpen = false;

  constructor(
    private router: Router,
    private adminService: AdminService,
    private breakService: BreakService,
    private userService: UserService, // 👇 INYECTADO AQUÍ
    private cdr: ChangeDetectorRef 
  ) {}

  ngOnInit() {
    this.userName = localStorage.getItem('fullName');
    this.role = localStorage.getItem('role');
    this.myUserId = localStorage.getItem('userId') || ''; 

    if (!localStorage.getItem('token') || (this.role !== 'ADMIN' && this.role !== 'SUPERADMIN')) {
      this.router.navigate(['/dashboard']);
      return;
    }

    this.loadProfile(); // 👇 CARGAMOS LA FOTO AL INICIAR
    this.loadStats();
    this.loadCategories();
  }

  toggleMobileMenu() { this.isMobileMenuOpen = !this.isMobileMenuOpen; }

  // 🪟 FUNCIONES DE MODALES
  openCategoryModal() { this.isCategoryModalOpen = true; }
  closeCategoryModal() { this.isCategoryModalOpen = false; }
  
  openBreakModal() { this.isBreakModalOpen = true; }
  closeBreakModal() { this.isBreakModalOpen = false; }

  openInviteModal() { this.isInviteModalOpen = true; }
  closeInviteModal() { this.isInviteModalOpen = false; }

  // 👇 LÓGICA DEL BOTÓN DE USUARIOS/EMPLEADOS
  manageUsers() {
    if (this.role === 'SUPERADMIN') {
      this.router.navigate(['/superadmin']); // Te lleva a tu panel global
    } else {
      this.showNotification('Módulo de plantilla de empleados en construcción.', false);
    }
  }

  // 👇 MÉTODO PARA CARGAR LA FOTO DEL USUARIO
  loadProfile() {
    if (!this.myUserId) return;
    this.userService.getProfile(this.myUserId).subscribe({
      next: (data) => {
        this.profilePic = data.profilePictureUrl;
        this.cdr.detectChanges(); 
      },
      error: (err) => console.error('Error cargando perfil:', err)
    });
  }

  loadStats() {
    this.adminService.getCompanyStats(this.myUserId).subscribe({
      next: (data) => {
        setTimeout(() => {
          this.companyName = data.companyName;
          this.totalEmployees = data.totalEmployees;
          this.totalBreaks = data.totalBreaksCompleted;
          this.totalCoins = data.totalCoinsEarned;
          this.isLoadingStats = false;
          this.cdr.detectChanges(); 
        });
      },
      error: (err) => {
        console.error('Error cargando estadísticas', err);
        setTimeout(() => {
          this.companyName = 'Error al cargar';
          this.isLoadingStats = false;
        });
      }
    });
  }

  loadCategories() {
    this.breakService.getCategories().subscribe({
      next: (data) => this.categories = data,
      error: (err) => console.error('Error cargando categorías', err)
    });
  }

  saveCategory() {
    this.breakService.createCategory(this.newCategory).subscribe({
      next: () => {
        this.showNotification('Categoría creada con éxito', false);
        this.newCategory = { name: '', description: '' };
        this.loadCategories();
        this.closeCategoryModal();
      },
      error: () => this.showNotification('Error al crear categoría', true)
    });
  }

  saveBreak() {
    if (!this.newBreak.categoryId) {
      this.showNotification('Por favor selecciona una categoría', true);
      return;
    }
    const payload: any = { ...this.newBreak };
    delete payload.categoryId;

    this.breakService.createBreak(payload, this.newBreak.categoryId, this.myUserId).subscribe({
      next: () => {
        this.showNotification('Pausa creada con éxito', false);
        this.newBreak = { title: '', description: '', durationSeconds: 60, mediaUrl: '', coinReward: 10, categoryId: null };
        this.closeBreakModal();
      },
      error: () => this.showNotification('Error al crear la pausa', true)
    });
  }

  sendInvite() {
    this.showNotification(`Invitación enviada a ${this.newInvite.email}`, false);
    this.newInvite.email = '';
    this.closeInviteModal();
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