import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BreakService {
  private apiUrl = 'http://localhost:8080/api/breaks';

  constructor(private http: HttpClient) { }

  // Método para traer todas las pausas activas del backend
  getAllBreaks(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  // === NUEVOS MÉTODOS ===

  // 2. Traer las estadísticas del usuario (El contador)
  getUserStats(userId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/stats/${userId}`);
  }

  // 3. Registrar que el usuario completó una pausa
  completeBreak(breakId: number, userId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${breakId}/complete/${userId}`, {});
  }
}