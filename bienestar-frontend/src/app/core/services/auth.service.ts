import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) { }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
      tap((response: any) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('fullName', response.fullName);
        localStorage.setItem('role', response.role);
      })
    );
  }

  // === NUEVO MÉTODO DE REGISTRO ===
  register(userData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, userData).pipe(
      tap((response: any) => {
        // Al registrarse exitosamente, el backend también devuelve un token, ¡así que lo guardamos de una vez!
        localStorage.setItem('token', response.token);
        localStorage.setItem('fullName', response.fullName);
        localStorage.setItem('role', response.role);
      })
    );
  }
}