import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { JwtStorage } from './jwt-storage';
import { Observable, tap } from 'rxjs';
import { TokenTransport } from '../interfaces/jwt-interface';
import { Role } from '../type/role';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  constructor(
    private http: HttpClient,
    private jwtStorage: JwtStorage
  ) {}

  login(username: string, password: string): Observable<TokenTransport>{
    return this.http.post<TokenTransport>("http://localhost:8080/api/user/login", {
      username,
      password
    }).pipe(
    tap(response => {
      if (response.token){
        this.jwtStorage.setToken(response.token);
      }
    })     
    );

  }

  logout():void {
    this.jwtStorage.clearToken();
  }


  getRole(): Role | null {
    const token = this.jwtStorage.getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      const role = payload.role; // "ROLE_CUSTOMER" or "ROLE_EMPLOYEE"
      if (role === 'CUSTOMER' || role === 'EMPLOYEE') return role;
      return null;

    } catch (err) {
      console.log(err);
      return null;
    }

  }
  



  
}
