import { Component, inject, OnInit } from '@angular/core';
import { NotificationService } from '../../services/notification/notification.service';
import { NgClass, NgFor, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
    selector: 'app-notification-popup',
    imports: [
        NgClass,
        NgFor,
        NgSwitchCase,
        NgSwitch,
        NgSwitchDefault,
        MatIconModule
    ],
    templateUrl: './notification-popup.component.html',
    styleUrl: './notification-popup.component.scss'
})
export class NotificationPopupComponent implements OnInit {
  messages: { type: 'success' | 'error', text: string }[] = [];

  notificationService = inject(NotificationService);

  ngOnInit(): void {
    this.notificationService.getMessages().subscribe(message => {
      this.messages.push(message);
      setTimeout(() => {
        this.messages.shift();
      }, 5000);
    });
  }
}