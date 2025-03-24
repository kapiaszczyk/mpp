import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PhotoViewerComponent } from './photo-viewer.component';

describe('PhotoViewerComponent', () => {
  let component: PhotoViewerComponent;
  let fixture: ComponentFixture<PhotoViewerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PhotoViewerComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PhotoViewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
