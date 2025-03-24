import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DuplicatePhotoDialogComponent } from './duplicate-photo-dialog.component';

describe('DuplicatePhotoDialogComponent', () => {
  let component: DuplicatePhotoDialogComponent;
  let fixture: ComponentFixture<DuplicatePhotoDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DuplicatePhotoDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DuplicatePhotoDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
