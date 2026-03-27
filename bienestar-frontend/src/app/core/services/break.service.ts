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
}