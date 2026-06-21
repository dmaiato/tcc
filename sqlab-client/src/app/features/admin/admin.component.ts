import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, NgIconsModule, RouterLink],
  templateUrl: './admin.component.html'
})
export class AdminComponent {}
