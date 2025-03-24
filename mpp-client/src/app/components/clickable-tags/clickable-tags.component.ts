import { NgClass, NgFor, NgIf } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { MatChipsModule } from '@angular/material/chips';
import { MatListModule } from '@angular/material/list';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-clickable-tags',
  imports: [
    MatChipsModule,
    NgIf,
    NgFor,
    MatListModule,
    TranslateModule,
    NgClass
  ],
  templateUrl: './clickable-tags.component.html',
  styleUrl: './clickable-tags.component.scss'
})
export class ClickableTagsComponent {
  @Input() tags: string[] = [];
  @Input() currentTag: string = '';

  maxTags: number = 15;
  showMore: boolean = false;

  router = inject(Router);

  navigateToTag(tag: string) {
    this.router.navigate(['/tag-gallery/', tag]);
  }

  get displayedTags(): string[] {
    return this.tags.slice(0, this.maxTags);
  }

  get remainingTags(): number {
    return this.tags.length - this.maxTags;
  }

  toggleShowMore(): void {
    this.showMore = !this.showMore;
    this.maxTags = this.showMore ? this.tags.length : 15;
  }

  toggleShowLess(): void {
    this.showMore = false;
    this.maxTags = 15;
  }
}
