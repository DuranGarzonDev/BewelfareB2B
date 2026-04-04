import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms'; 
import { BreakService } from '../../core/services/break.service';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule], 
  templateUrl: './admin-panel.component.html'
})
export class AdminPanelComponent implements OnInit {
  userName: string | null = '';
  role: string | null = '';
  categories: any[] = [];
  
  myUserId: string = '';

  toastMessage = '';
  showToast = false;
  isError = false;

  newCategory = { name: '', description: '' };
  // 👇 NUEVO: Añadido coinReward con valor por defecto 10
  newBreak = { title: '', description: '', durationSeconds: 60, mediaUrl: '', categoryId: null, coinReward: 10 };
  isMobileMenuOpen: boolean = false;

  constructor(
    private router: Router,
    private breakService: BreakService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.userName = localStorage.getItem('fullName');
    this.role = localStorage.getItem('role');
    this.myUserId = localStorage.getItem('userId') || '';

    if (this.role !== 'ADMIN' && this.role !== 'SUPERADMIN') {
      this.router.navigate(['/dashboard']);
      return;
    }

    this.loadCategories();
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  loadCategories() {
    this.breakService.getCategories().subscribe({
      next: (data) => {
        this.categories = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Error cargando categorías:", err)
    });
  }

  saveCategory() {
    if (!this.newCategory.name) return;

    this.breakService.createCategory(this.newCategory).subscribe({
      next: () => {
        this.showNotification('Categoría creada con éxito', false);
        this.newCategory = { name: '', description: '' }; 
        this.loadCategories(); 
      },
      error: () => this.showNotification('Error al crear la categoría', true)
    });
  }

  saveBreak() {
    if (!this.newBreak.title || !this.newBreak.categoryId) return;

    const payload = {
      title: this.newBreak.title,
      description: this.newBreak.description,
      durationSeconds: Number(this.newBreak.durationSeconds),
      mediaUrl: this.newBreak.mediaUrl,
      // 👇 NUEVO: Enviamos las monedas al backend
      coinReward: Number(this.newBreak.coinReward) 
    };

    const categoryId = Number(this.newBreak.categoryId);

    this.breakService.createBreak(payload, categoryId, this.myUserId).subscribe({
      next: () => {
        this.showNotification('Pausa Activa guardada correctamente', false);
        // 👇 NUEVO: Reiniciamos el formulario incluyendo las monedas
        this.newBreak = { title: '', description: '', durationSeconds: 60, mediaUrl: '', categoryId: null, coinReward: 10 };
      },
      error: (err) => {
        console.error("Error completo del backend:", err);
        this.showNotification('Error al guardar la pausa', true);
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