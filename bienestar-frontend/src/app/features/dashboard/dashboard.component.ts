import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { trigger, style, animate, transition } from '@angular/animations';
import { BreakService } from '../../core/services/break.service'; 

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  animations: [
    trigger('toastAnimation', [
      transition(':enter', [
        style({ transform: 'translateX(100%)', opacity: 0 }),
        animate('300ms ease-out', style({ transform: 'translateX(0)', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ transform: 'translateX(100%)', opacity: 0 }))
      ])
    ])
  ]
})
export class DashboardComponent implements OnInit {
  userName: string | null = '';
  role: string | null = '';
  showToast = false;
  toastMessage = '';

  activeBreaks: any[] = []; 
  completedBreaksCount: number = 0;
  currentStreak: number = 0;
  
  // 👇 INICIA VACÍO
  myUserId: string = ''; 

  // === VARIABLES DEL MODAL Y RELOJ ===
  isModalOpen = false;
  currentBreak: any = null;
  timeLeft: number = 0;
  timerInterval: any;

  constructor(
    private router: Router, 
    private cdr: ChangeDetectorRef,
    private breakService: BreakService 
  ) {}

  ngOnInit() {
    this.userName = localStorage.getItem('fullName');
    this.role = localStorage.getItem('role');
    
    // 👇 LO ATRAPAMOS DINÁMICAMENTE AQUÍ
    this.myUserId = localStorage.getItem('userId') || ''; 

    if (!localStorage.getItem('token')) {
      this.router.navigate(['/login']);
    } else {
      this.toastMessage = `¡Bienvenido, ${this.userName}!`;
      this.showToast = true;

      setTimeout(() => {
        this.showToast = false;
        this.cdr.detectChanges(); 
      }, 2500);

      this.loadBreaks();
      this.loadStats();
    }
  }

  loadBreaks() {
    this.breakService.getAllBreaks().subscribe({
      next: (data: any[]) => this.activeBreaks = data,
      error: (err: any) => console.error('Error cargando pausas:', err)
    });
  }

  loadStats() {
    // Pequeña validación de seguridad
    if (!this.myUserId) return; 

    this.breakService.getUserStats(this.myUserId).subscribe({
      next: (stats: any) => {
        this.completedBreaksCount = stats.pausasCompletadas;
        this.currentStreak = stats.rachaDias; 
      },
      error: (err: any) => console.error('Error cargando estadísticas:', err)
    });
  }

  logout() {
    localStorage.clear(); 
    this.router.navigate(['/login']); 
  }

  // === LÓGICA DEL TEMPORIZADOR ===
  openModal(breakItem: any) {
    this.currentBreak = breakItem;
    this.timeLeft = breakItem.durationSeconds; 
    this.isModalOpen = true;
    this.startTimer();
  }

  closeModal() {
    this.isModalOpen = false;
    this.currentBreak = null;
    if (this.timerInterval) {
      clearInterval(this.timerInterval); 
    }
  }

  startTimer() {
    this.timerInterval = setInterval(() => {
      if (this.timeLeft > 0) {
        this.timeLeft--;
        this.cdr.detectChanges(); 
      } else {
        clearInterval(this.timerInterval);
        this.finishBreak();
      }
    }, 700); 
  }

  finishBreak() {
    if (!this.myUserId) return;

    this.breakService.completeBreak(this.currentBreak.id, this.myUserId).subscribe({
      next: () => {
        this.closeModal();
        this.loadStats(); 
        
        this.toastMessage = `¡Excelente! Pausa completada.`;
        this.showToast = true;
        setTimeout(() => { this.showToast = false; this.cdr.detectChanges(); }, 3000);
      },
      error: (err) => console.error("Error guardando historial:", err)
    });
  }
}