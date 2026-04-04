import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { BreakService } from '../../core/services/break.service';
import { UserService } from '../../core/services/user.service'; // 👇 Importar

@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './statistics.component.html'
})
export class StatisticsComponent implements OnInit {
  myUserId: string = '';
  userName: string | null = '';
  role: string | null = '';
  isLoading: boolean = true;

  // Variables para las Estadísticas
  totalBreaks: number = 0;
  totalMinutes: number = 0;
  favoriteCategory: string = 'Ninguna';
  
  // Datos para nuestro gráfico de barras
  categoryStats: { name: string, count: number, percentage: number }[] = [];
  isMobileMenuOpen: boolean = false;
  profilePic: string | null = null;

  constructor(
    private breakService: BreakService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private userService: UserService
  ) {}

  ngOnInit() {
    this.userName = localStorage.getItem('fullName');
    this.role = localStorage.getItem('role');
    this.myUserId = localStorage.getItem('userId') || '';

    if (!localStorage.getItem('token')) {
      this.router.navigate(['/login']);
    } else if (this.myUserId) {
      this.loadAndCalculateStats();
      this.userService.getProfile(this.myUserId).subscribe({
        next: (data) => {
          this.profilePic = data.profilePictureUrl;
          this.cdr.detectChanges(); // Actualizar la vista
        }
      });
    }
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  loadAndCalculateStats() {
    this.breakService.getUserHistory(this.myUserId).subscribe({
      next: (history: any[]) => {
        this.totalBreaks = history.length;
        
        if (this.totalBreaks > 0) {
          // 1. Calcular el tiempo total en minutos
          const totalSeconds = history.reduce((sum, item) => sum + item.durationSeconds, 0);
          this.totalMinutes = Math.round(totalSeconds / 60);

          // 2. Contar cuántas pausas hay por categoría
          const categoryCounts: any = {};
          history.forEach(item => {
            const cat = item.categoryName || 'General';
            categoryCounts[cat] = (categoryCounts[cat] || 0) + 1;
          });

          // 3. Transformar ese conteo en un arreglo para el HTML y calcular porcentajes
          this.categoryStats = Object.keys(categoryCounts).map(key => {
            return {
              name: key,
              count: categoryCounts[key],
              percentage: Math.round((categoryCounts[key] / this.totalBreaks) * 100)
            };
          }).sort((a, b) => b.count - a.count); // Ordenar de mayor a menor

          // 4. Sacar la categoría favorita (la primera después de ordenar)
          this.favoriteCategory = this.categoryStats[0].name;
        }

        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando estadísticas:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  logout() {
    localStorage.clear(); 
    this.router.navigate(['/login']); 
  }
}