import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private messages = new Subject<{ type: 'success' | 'error', text: string }>();

  constructor(private translate: TranslateService) { }

  private sendMessage(type: 'success' | 'error', text: string): void {
    this.messages.next({ 
      type, 
      text: this.translate.instant(text)
    });
  }

  sendSuccessMessage(text: string): void {
    this.sendMessage('success', text);
  }

  sendErrorMessage(text: string): void {
    this.sendMessage('error', text);
  }

  getMessages(): Observable<{ type: 'success' | 'error', text: string }> {
    return this.messages.asObservable();
  }
}