import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
// 1. Importamos las herramientas de animación
import { trigger, style, animate, transition, state } from '@angular/animations';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  // 2. DEFINIMOS LA ANIMACIÓN AQUÍ
  animations: [
    trigger('toastAnimation', [
      // ESTADO DE ENTRADA (:enter)
      // Comienza invisible y fuera de la pantalla a la derecha (translateX 100%)
      transition(':enter', [
        style({ transform: 'translateX(100%)', opacity: 0 }),
        // Se anima durante 300ms hasta su posición normal
        animate('300ms ease-out', style({ transform: 'translateX(0)', opacity: 1 }))
      ]),
      // ESTADO DE SALIDA (:leave)
      // Cuando se va a ir, se anima desde su posición normal hacia la derecha
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

  constructor(private router: Router, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.userName = localStorage.getItem('fullName');
    this.role = localStorage.getItem('role');

    if (!localStorage.getItem('token')) {
      this.router.navigate(['/login']);
    } else {
      this.toastMessage = `¡Bienvenido, ${this.userName}!`;
      this.showToast = true;

      // Aumenté un poquito el tiempo a 2.5s para apreciar la animación de entrada y salida
      setTimeout(() => {
        this.showToast = false;
        this.cdr.detectChanges(); 
      }, 2500);
    }
  }

  logout() {
    localStorage.clear(); 
    this.router.navigate(['/login']); 
  }
}