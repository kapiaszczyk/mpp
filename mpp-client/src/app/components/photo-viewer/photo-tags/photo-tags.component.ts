import { Component, EventEmitter, Input, input, NgModule, Output, ChangeDetectionStrategy, ViewChild, ElementRef } from '@angular/core';
import { MatChipEditedEvent, MatChipInputEvent, MatChipsModule } from '@angular/material/chips';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { NgForOf } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-photo-tags',
  imports: [
    MatChipsModule,
    TranslateModule,
    NgForOf,
    MatIconModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
  ],
  templateUrl: './photo-tags.component.html',
  styleUrl: './photo-tags.component.scss',
})
export class PhotoTagsComponent {
  @Input() tags: string[] = [];
  @Input() editable = false;
  @Output() tagsChanged = new EventEmitter<string[]>();

  readonly separatorKeysCodes = [ENTER, COMMA] as const;
  readonly addOnBlur = true;

  addTag(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();
    if (value && !this.tags.includes(value)) {
      this.tags.push(value);
      this.tagsChanged.emit([...this.tags]);
    }
    event.chipInput!.clear();
  }

  removeTag(index: number): void {
    this.tags.splice(index, 1);
    this.tagsChanged.emit([...this.tags]);
  }

  editTag(index: number, event: MatChipEditedEvent): void {
    const value = event.value.trim();
    if (!value) {
      this.removeTag(index);
      return;
    }

    this.tags[index] = value;
    this.tagsChanged.emit([...this.tags]);
  }

  // Prevent the parent component from handling certain keydown events 
  // (which are used there to close and navigate the viewer)
  handleKeydown(event: KeyboardEvent): void {
    if (['ArrowLeft', 'ArrowRight', 'i'].includes(event.key)) {
      event.stopPropagation();
    }

    // Prevents the parent component from handling the Escape key
    // but still allows the input to lose focus
    if (event.key === 'Escape') {
      event.stopPropagation();
      (event.target as HTMLElement).blur();
    }
  }



}