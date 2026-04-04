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
  leaderboard: any[] = []; 
  streakStatus: any = null; 
  
  completedBreaksCount: number = 0;
  currentStreak: number = 0;
  coins: number = 0; 
  
  myUserId: string = ''; 

  // === VARIABLES DE MODALES ===
  isModalOpen = false;
  currentBreak: any = null;
  timeLeft: number = 0;
  timerInterval: any;

  isStreakModalOpen = false;
  streakWasJustActivated = false;

  isMobileMenuOpen: boolean = false;
  profilePic: string | null = null;

  // 👇 MOTOR DE AUDIO GLOBAL 👇
  private audioCtx: any = null;

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
    this.loadStreakStatus();
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  loadProfile() {
    if (!this.myUserId) return;
    this.userService.getProfile(this.myUserId).subscribe({
      next: (data) => {
        this.profilePic = data.profilePictureUrl;
        this.coins = data.coins || 0; 
        this.currentStreak = data.currentStreak || 0; 
        this.cdr.detectChanges(); 
      }
    });
  }

  loadStreakStatus() {
    if (!this.myUserId) return;
    this.userService.getStreakStatus(this.myUserId).subscribe({
      next: (status) => {
        const hadAlreadyBrokenToday = this.streakStatus?.type === 'SUCCESS';
        this.streakStatus = status;
        
        if (!hadAlreadyBrokenToday && status.type === 'SUCCESS') {
           this.streakWasJustActivated = true;
        }

        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error cargando estado de racha:', err)
    });
  }

  loadLeaderboard() {
    if (!this.myUserId) return; 
    
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
      },
      error: (err: any) => console.error('Error cargando estadísticas:', err)
    });
  }

  logout() {
    localStorage.clear(); 
    this.router.navigate(['/login']); 
  }

  // 👇 INICIALIZAR AUDIO (Se llama con el clic del usuario)
  initAudio() {
    if (!this.audioCtx) {
      this.audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
    }
    // Si el navegador lo suspendió, lo despertamos
    if (this.audioCtx.state === 'suspended') {
      this.audioCtx.resume();
    }
  }

  openModal(breakItem: any) {
    this.initAudio(); // 🔓 DESBLOQUEAMOS EL AUDIO CON EL CLIC
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

  // 👇 REPRODUCIR SONIDO (Estilo Moneda / Campanita)
  playSuccessSound() {
    if (!this.audioCtx) return;

    const oscillator = this.audioCtx.createOscillator();
    const gainNode = this.audioCtx.createGain();

    oscillator.connect(gainNode);
    gainNode.connect(this.audioCtx.destination);

    oscillator.type = 'sine';
    
    // Tono 1 y Tono 2 (Hace "Ti-Ding!")
    oscillator.frequency.setValueAtTime(987.77, this.audioCtx.currentTime); // Nota B5
    oscillator.frequency.setValueAtTime(1318.51, this.audioCtx.currentTime + 0.1); // Nota E6

    // Volumen de campana (Sube rápido y baja suave)
    gainNode.gain.setValueAtTime(0, this.audioCtx.currentTime);
    gainNode.gain.linearRampToValueAtTime(0.5, this.audioCtx.currentTime + 0.05);
    gainNode.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + 0.5);

    oscillator.start(this.audioCtx.currentTime);
    oscillator.stop(this.audioCtx.currentTime + 0.6);
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
    }, 1000); // Lo cambié a 1000ms (1 segundo exacto) para que el reloj sea real.
  }

  finishBreak() {
    if (!this.myUserId) return;

    this.breakService.completeBreak(this.currentBreak.id, this.myUserId).subscribe({
      next: () => {
        this.closeModal();
        this.playSuccessSound(); // 🔔 ¡AHORA SÍ SUENA EL DING!
        
        const wasFirstBreakOfDay = this.streakStatus?.type !== 'SUCCESS';

        this.loadStats(); 
        this.loadProfile(); 
        this.loadLeaderboard(); 
        this.loadStreakStatus(); 

        if (wasFirstBreakOfDay) {
          this.isStreakModalOpen = true;
        } else {
           this.toastMessage = `¡Excelente! +${this.currentBreak.coinReward || 10} Coins ganados.`;
           this.showToast = true;
           setTimeout(() => { this.showToast = false; this.cdr.detectChanges(); }, 3000);
        }
      },
      error: (err) => console.error("Error guardando historial:", err)
    });
  }

  closeStreakModal() {
    this.isStreakModalOpen = false;
    this.streakWasJustActivated = false;
  }
}