import { Component, OnInit, ChangeDetectorRef } from '@angular/core'; // 1. 👇 Importamos ChangeDetectorRef
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router'; // 2. 👇 Importamos para navegación y menú
import { BreakService } from '../../core/services/break.service';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, RouterModule], // 3. 👇 Asegúrate de tenerRouterModule aquí
  templateUrl: './history.component.html'
})
export class HistoryComponent implements OnInit {
  breakHistory: any[] = [];
  myUserId: string = '';
  isLoading: boolean = true;
  
  // 4. 👇 Variables para poblar el menú lateral y header
  userName: string | null = '';
  role: string | null = '';

  isMobileMenuOpen: boolean = false;

  constructor(
    private breakService: BreakService,
    private router: Router, // Para el logout
    private cdr: ChangeDetectorRef // 5. 👇 Inyectamos el pellizco mágico
  ) {}

  ngOnInit() {
    // 6. 👇 Recuperamos todos los datos del usuario logueado
    this.userName = localStorage.getItem('fullName');
    this.role = localStorage.getItem('role');
    this.myUserId = localStorage.getItem('userId') || '';

    if (!localStorage.getItem('token')) {
      this.router.navigate(['/login']);
    } else if (this.myUserId) {
      this.loadHistory();
    } else {
      this.isLoading = false;
      this.cdr.detectChanges(); // Forzamos actualización visual
    }
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  loadHistory() {
    this.breakService.getUserHistory(this.myUserId).subscribe({
      next: (data) => {
        this.breakHistory = data;
        this.isLoading = false;
        
        // 7. 👇 EL PELLIZCO MÁGICO SENIOR: 
        // A veces Standalone components no detectan rápido el fin de carga. Esto lo arregla.
        this.cdr.detectChanges(); 
      },
      error: (err) => {
        console.error('Error cargando el historial:', err);
        this.isLoading = false;
        // 👇 También lo forzamos en error para que deje de girar
        this.cdr.detectChanges(); 
      }
    });
  }

  logout() {
    localStorage.clear(); 
    this.router.navigate(['/login']); 
  }
}