import { Component } from '@angular/core';
import { Espectaculos } from './espectaculos/espectaculos';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [Espectaculos, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}