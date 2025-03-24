import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PhotoSelectionBarComponent } from './photo-selection-bar.component';

describe('PhotoSelectionBarComponent', () => {
  let component: PhotoSelectionBarComponent;
  let fixture: ComponentFixture<PhotoSelectionBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PhotoSelectionBarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PhotoSelectionBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
