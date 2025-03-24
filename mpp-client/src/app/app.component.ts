import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TranslateService } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { TranslateModule } from '@ngx-translate/core';
import { RouterOutlet } from '@angular/router';
import { NotificationPopupComponent } from './components/notification-popup/notification-popup.component';
import { OverlayContainer } from '@angular/cdk/overlay';
import { environment } from '../environments/environment';

export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

@Component({
    selector: 'app-root',
    imports: [RouterOutlet, NotificationPopupComponent, TranslateModule],
    templateUrl: './app.component.html',
    styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'mpp-frontend';

  private defaultLanguage = environment.defaultLanguage;
  constructor(private translate: TranslateService, private overlayContainer: OverlayContainer) {
    this.translate.setDefaultLang(this.defaultLanguage);
    this.translate.use(this.defaultLanguage);
  }

  changeLanguage(language: string) {
    this.translate.use(language);
  }
  
  // TODO: This doesnt work
  changeTheme(theme: 'light-theme' | 'dark-theme') {
    const overlayContainerClasses = this.overlayContainer.getContainerElement().classList;
    const themeClassesToRemove = Array.from(overlayContainerClasses)
      .filter((item: string) => item.includes('-theme'))
    if (themeClassesToRemove.length) {
      overlayContainerClasses.remove(...themeClassesToRemove);
    }
    overlayContainerClasses.add(theme);
  }
}
