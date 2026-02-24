import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { trigger, style, animate, transition } from '@angular/animations';
import { BreakService } from '../../core/services/break.service'; 

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
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
  myUserId: string = '4985ab5a-8646-412c-b853-032c7ef614e4'; // Tu UUID

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
    this.breakService.getUserStats(this.myUserId).subscribe({
      next: (stats: any) => this.completedBreaksCount = stats.pausasCompletadas,
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
    this.timeLeft = breakItem.durationSeconds; // Tomamos los segundos de la BD
    this.isModalOpen = true;
    this.startTimer();
  }

  closeModal() {
    this.isModalOpen = false;
    this.currentBreak = null;
    if (this.timerInterval) {
      clearInterval(this.timerInterval); // Apagamos el reloj si cierran la ventana
    }
  }

  startTimer() {
    this.timerInterval = setInterval(() => {
      if (this.timeLeft > 0) {
        this.timeLeft--;
        // 👇 EL PELLIZCO MÁGICO: Le decimos a Angular que actualice el número en pantalla CADA SEGUNDO
        this.cdr.detectChanges(); 
      } else {
        // Cuando llega a 0
        clearInterval(this.timerInterval);
        this.finishBreak();
      }
    }, 700); // 1000 milisegundos = 1 segundo
  }

  finishBreak() {
    // Le avisamos a Spring Boot que terminamos
    this.breakService.completeBreak(this.currentBreak.id, this.myUserId).subscribe({
      next: () => {
        this.closeModal();
        this.loadStats(); // Recargamos el contador del Dashboard (Magia pura)
        
        // Disparamos un Toast de felicitación
        this.toastMessage = `¡Excelente! Pausa completada.`;
        this.showToast = true;
        setTimeout(() => { this.showToast = false; this.cdr.detectChanges(); }, 3000);
      },
      error: (err) => console.error("Error guardando historial:", err)
    });
  }
}