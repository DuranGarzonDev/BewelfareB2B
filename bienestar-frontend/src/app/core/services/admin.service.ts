import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) { }

  private getHeaders() {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  // ==========================================
  // 📊 OBTENER ESTADÍSTICAS GLOBALES
  // ==========================================
  getCompanyStats(adminId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${adminId}/company-stats`, { headers: this.getHeaders() });
  }

  // ==========================================
  // 📩 ENVIAR INVITACIÓN A EMPLEADO
  // ==========================================
  inviteUser(adminId: string, email: string): Observable<any> {
    return this.http.post<any>(
      `${this.apiUrl}/${adminId}/invites`, 
      { email: email }, 
      { headers: this.getHeaders() }
    );
  }
}