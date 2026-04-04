import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SuperadminService {
  private apiUrl = 'http://localhost:8080/api/superadmin';

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  // --- EMPRESAS ---
  getCompanies(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/companies`, { headers: this.getHeaders() });
  }

  createCompany(companyData: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/companies`, companyData, { headers: this.getHeaders() });
  }

  // --- USUARIOS ---
  getUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/users`, { headers: this.getHeaders() });
  }

  changeUserRole(userId: string, role: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/users/${userId}/role?role=${role}`, {}, { headers: this.getHeaders() });
  }
}