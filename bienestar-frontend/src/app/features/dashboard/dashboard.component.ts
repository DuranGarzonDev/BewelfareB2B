import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { trigger, style, animate, transition } from '@angular/animations';
import { UserService } from '../../core/services/user.service'; 
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
  leaderboard: any[] = []; // 🏆 Nuestro Top 10
  
  completedBreaksCount: number = 0;
  currentStreak: number = 0;
  coins: number = 0; // 🪙 Billetera del usuario
  
  myUserId: string = ''; 

  // === VARIABLES DEL MODAL Y RELOJ ===
  isModalOpen = false;
  currentBreak: any = null;
  timeLeft: number = 0;
  timerInterval: any;

  isMobileMenuOpen: boolean = false;
  profilePic: string | null = null;

  constructor(
    private router: Router, 
    private cdr: ChangeDetectorRef,
    private breakService: BreakService,
    private userService: UserService
  ) {}

  ngOnInit() {
    this.userName = localStorage.getItem('fullName');
    this.role = localStorage.getItem('role');
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

      this.loadAllData();
    }
  }

  loadAllData() {
    this.loadBreaks();
    this.loadStats();
    this.loadProfile();
    this.loadLeaderboard();
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  loadProfile() {
    if (!this.myUserId) return;
    this.userService.getProfile(this.myUserId).subscribe({
      next: (data) => {
        this.profilePic = data.profilePictureUrl;
        this.coins = data.coins || 0; // 🪙 Extraemos las monedas de la BD
        this.currentStreak = data.currentStreak || 0; // 🔥 Extraemos la racha oficial
        this.cdr.detectChanges(); 
      }
    });
  }

  loadLeaderboard() {
    if (!this.myUserId) return; // Validación de seguridad
    
    // 👇 Le pasamos el ID al servicio
    this.userService.getLeaderboard(this.myUserId).subscribe({
      next: (data) => {
        this.leaderboard = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error cargando Leaderboard:', err)
    });
  }

  loadBreaks() {
    if (!this.myUserId) return;
    this.breakService.getAllBreaks(this.myUserId).subscribe({
      next: (data: any[]) => this.activeBreaks = data,
      error: (err: any) => console.error('Error cargando pausas:', err)
    });
  }

  loadStats() {
    if (!this.myUserId) return; 
    this.breakService.getUserStats(this.myUserId).subscribe({
      next: (stats: any) => {
        this.completedBreaksCount = stats.pausasCompletadas;
        // Ya no usamos la racha de aquí, usamos la del Profile que es más precisa
      },
      error: (err: any) => console.error('Error cargando estadísticas:', err)
    });
  }

  logout() {
    localStorage.clear(); 
    this.router.navigate(['/login']); 
  }

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
        
        // RECARGAMOS TODO PARA VER LA MAGIA EN TIEMPO REAL 🚀
        this.loadStats(); 
        this.loadProfile(); 
        this.loadLeaderboard(); 
        
        this.toastMessage = `¡Excelente! +${this.currentBreak.coinReward || 10} Coins ganados.`;
        this.showToast = true;
        setTimeout(() => { this.showToast = false; this.cdr.detectChanges(); }, 3000);
      },
      error: (err) => console.error("Error guardando historial:", err)
    });
  }
}