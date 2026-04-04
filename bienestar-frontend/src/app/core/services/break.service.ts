import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http'; 
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BreakService {
  private apiUrl = 'http://localhost:8080/api/breaks';

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  // 👇 ACTUALIZADO: Ahora recibe el userId para filtrar por empresa
  getAllBreaks(userId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${userId}`, { headers: this.getHeaders() });
  }

  getUserStats(userId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/stats/${userId}`, { headers: this.getHeaders() });
  }

  completeBreak(breakId: number, userId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${breakId}/complete/${userId}`, {}, { headers: this.getHeaders() });
  }

  getUserHistory(userId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/history/${userId}`, { headers: this.getHeaders() });
  }

  // === MÉTODOS DEL ADMINISTRADOR ===

  getCategories(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/categories`, { headers: this.getHeaders() });
  }

  createCategory(category: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/categories`, category, { headers: this.getHeaders() });
  }

  // 👇 ACTUALIZADO: Ahora recibe el creatorId para saber a qué empresa asignar la pausa
  createBreak(breakData: any, categoryId: number, creatorId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${creatorId}?categoryId=${categoryId}`, breakData, { headers: this.getHeaders() });
  }
}