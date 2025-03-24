import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TagGalleryComponent } from './tag-gallery.component';

describe('TagGalleryComponent', () => {
  let component: TagGalleryComponent;
  let fixture: ComponentFixture<TagGalleryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TagGalleryComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TagGalleryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
