import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http'; // 1. Importamos HttpHeaders
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:8080/api/users'; // La ruta de tu nuevo controlador

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}` // Inyectamos el pase VIP
    });
  }

  // 1. Pedir el perfil a Java
  getProfile(userId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/profile/${userId}`, { headers: this.getHeaders() });
  }

  // 2. Mandar los cambios a Java
  updateProfile(userId: string, profileData: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/profile/${userId}`, profileData, { headers: this.getHeaders() });
  }

  // ==========================================
  // 🏆 TRAER EL LEADERBOARD (Aislado)
  // ==========================================
  getLeaderboard(userId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/leaderboard/${userId}`, { headers: this.getHeaders() });
  }
}