import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms'; // <-- IMPORTANTE PARA LOS FORMULARIOS
import { BreakService } from '../../core/services/break.service';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule], // <-- AGREGAR AQUÍ
  templateUrl: './admin-panel.component.html'
})
export class AdminPanelComponent implements OnInit {
  userName: string | null = '';
  role: string | null = '';
  categories: any[] = [];
  
  // Variables para los mensajes de éxito/error
  toastMessage = '';
  showToast = false;
  isError = false;

  // Modelos para los formularios
  newCategory = { name: '', description: '' };
  newBreak = { title: '', description: '', durationSeconds: 60, mediaUrl: '', categoryId: null };

  constructor(
    private router: Router,
    private breakService: BreakService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.userName = localStorage.getItem('fullName');
    this.role = localStorage.getItem('role');

    // PROTECCIÓN DE RUTA: Si no es ADMIN, lo pateamos al Dashboard
    if (this.role !== 'ADMIN') {
      this.router.navigate(['/dashboard']);
      return;
    }

    this.loadCategories();
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
        this.newCategory = { name: '', description: '' }; // Limpiar formulario
        this.loadCategories(); // Recargar la lista
      },
      error: () => this.showNotification('Error al crear la categoría', true)
    });
  }

saveBreak() {
    if (!this.newBreak.title || !this.newBreak.categoryId) return;

    // 1. Armamos el JSON SOLO con los datos de la pausa (como lo hacías en Postman)
    const payload = {
      title: this.newBreak.title,
      description: this.newBreak.description,
      durationSeconds: Number(this.newBreak.durationSeconds),
      mediaUrl: this.newBreak.mediaUrl
    };

    // 2. Separamos el ID de la categoría para mandarlo por la URL
    const categoryId = Number(this.newBreak.categoryId);

    // 3. Llamamos al servicio con ambos datos
    this.breakService.createBreak(payload, categoryId).subscribe({
      next: () => {
        this.showNotification('Pausa Activa guardada correctamente', false);
        this.newBreak = { title: '', description: '', durationSeconds: 60, mediaUrl: '', categoryId: null };
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