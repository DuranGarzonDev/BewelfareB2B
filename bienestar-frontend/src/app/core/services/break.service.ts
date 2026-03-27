import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http'; // 1. Importamos HttpHeaders
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BreakService {
  private apiUrl = 'http://localhost:8080/api/breaks';

  constructor(private http: HttpClient) { }

  // 2. Método auxiliar para agarrar el pase VIP de la billetera (localStorage)
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  // 3. Le adjuntamos los headers a cada petición
  getAllBreaks(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  getUserStats(userId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/stats/${userId}`, { headers: this.getHeaders() });
  }

  completeBreak(breakId: number, userId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${breakId}/complete/${userId}`, {}, { headers: this.getHeaders() });
  }

  // === NUEVO MÉTODO: Traer el historial detallado ===
  getUserHistory(userId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/history/${userId}`, { headers: this.getHeaders() });
  }

  // === MÉTODOS DEL ADMINISTRADOR ===

  // 1. Traer todas las categorías para el select del formulario
  // 1. Traer todas las categorías (El que ya arreglaste)
  getCategories(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/categories`, { headers: this.getHeaders() });
  }

  // 2. Crear una nueva categoría (Ajustamos la ruta aquí)
  createCategory(category: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/categories`, category, { headers: this.getHeaders() });
  }

  // 3. Crear una nueva pausa activa (AHORA ENVÍA EL ID POR URL)
  createBreak(breakData: any, categoryId: number): Observable<any> {
    // Mandamos el JSON normal en el body, y le pegamos el ?categoryId= al final de la URL
    return this.http.post<any>(`${this.apiUrl}?categoryId=${categoryId}`, breakData, { headers: this.getHeaders() });
  }
}