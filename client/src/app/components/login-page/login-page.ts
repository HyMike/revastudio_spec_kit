import { Component, signal } from '@angular/core';
import { AuthService } from '../../services/auth-service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login-page',
  imports: [FormsModule],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css',
})
export class LoginPage {

  constructor(
    private authService: AuthService,
    private router: Router
  ){}

  username: string = ""; 
  password: string = "";
  failedLoginMessage = signal("");


  validateLogin(){
    this.failedLoginMessage.set("");

    if (!this.username || !this.password){
      this.failedLoginMessage.set("Please enter both username and password");
      return;
    }

    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.failedLoginMessage.set("");
        this.router.navigate([this.authService.getDashboardRoute()]);
      },
      error: (err) => {
        console.error("login error", err);
        this.failedLoginMessage.set("Invalid username and password");

      }
    
    })





  }



}
