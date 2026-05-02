import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth } from './auth';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  isLoggedIn$: any;

  constructor(public auth: Auth) {
    this.isLoggedIn$ = this.auth.token$;
  }

  logout() {
    this.auth.logout();
  }
}