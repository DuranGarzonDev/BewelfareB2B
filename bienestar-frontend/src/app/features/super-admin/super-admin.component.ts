import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SuperadminService } from '../../core/services/superadmin.service';

@Component({
  selector: 'app-super-admin',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './super-admin.component.html'
})
export class SuperAdminComponent implements OnInit {
  userName: string | null = '';
  role: string | null = '';
  isMobileMenuOpen: boolean = false;

  // Notificaciones
  showToast = false;
  toastMessage = '';
  isError = false;

  // Datos
  companies: any[] = [];
  users: any[] = [];
  
  // Formulario
  newCompany = { name: '', emailDomain: '' };

  constructor(
    private router: Router,
    private superadminService: SuperadminService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.userName = localStorage.getItem('fullName');
    this.role = localStorage.getItem('role');

    // PROTECCIÓN DE RUTA EXTREMA: Solo el dueño entra aquí
    if (this.role !== 'SUPERADMIN') {
      this.router.navigate(['/dashboard']);
      return;
    }

    this.loadCompanies();
    this.loadUsers();
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  loadCompanies() {
    this.superadminService.getCompanies().subscribe({
      next: (data) => {
        this.companies = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error cargando empresas:', err)
    });
  }

  loadUsers() {
    this.superadminService.getUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error cargando usuarios:', err)
    });
  }

  saveCompany() {
    if (!this.newCompany.name || !this.newCompany.emailDomain) return;

    // Limpiamos el dominio por si el usuario le pone el "@"
    let domain = this.newCompany.emailDomain.trim().toLowerCase();
    if (domain.startsWith('@')) domain = domain.substring(1);

    const payload = {
      name: this.newCompany.name,
      emailDomain: domain
    };

    this.superadminService.createCompany(payload).subscribe({
      next: () => {
        this.showNotification('Corporación creada exitosamente', false);
        this.newCompany = { name: '', emailDomain: '' };
        this.loadCompanies();
      },
      error: (err) => {
        console.error(err);
        this.showNotification('Error al crear la corporación (¿Dominio duplicado?)', true);
      }
    });
  }

  updateRole(userId: string, newRole: string) {
    this.superadminService.changeUserRole(userId, newRole).subscribe({
      next: () => {
        this.showNotification(`Rol actualizado a ${newRole}`, false);
        this.loadUsers(); // Recargamos para ver el cambio
      },
      error: (err) => {
        console.error(err);
        this.showNotification('Error al cambiar el rol', true);
      }
    });
  }

  showNotification(message: string, isError: boolean) {
    this.toastMessage = message;
    this.isError = isError;
    this.showToast = true;
    setTimeout(() => { this.showToast = false; this.cdr.detectChanges(); }, 3000);
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}