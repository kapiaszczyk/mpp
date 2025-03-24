import { NgForOf } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-language-selector',
  imports: [
    MatSelectModule,
    MatButtonModule,
    TranslateModule,
    NgForOf
  ],
  templateUrl: './language-selector.component.html',
  styleUrl: './language-selector.component.scss'
})
export class LanguageSelectorComponent implements OnInit {
  languages = [
    { code: 'en', label: 'English' },
    { code: 'pl', label: 'Polski' },
  ];

  selectedLanguage: string = 'en';
  translateService = inject(TranslateService);

  ngOnInit() {
    const savedLanguage = localStorage.getItem('language') || 'pl';
    this.selectedLanguage = savedLanguage;
    
    this.translateService.use(this.selectedLanguage);
  }

  onLanguageChange(event: MatSelectChange) {
    const language = event.value;
    this.selectedLanguage = language;

    localStorage.setItem('language', language);
    this.translateService.use(language);
  }
}
