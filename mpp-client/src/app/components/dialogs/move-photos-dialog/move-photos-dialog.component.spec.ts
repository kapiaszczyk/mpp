import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MovePhotosDialogComponent } from './move-photos-dialog.component';

describe('MovePhotosDialogComponent', () => {
  let component: MovePhotosDialogComponent;
  let fixture: ComponentFixture<MovePhotosDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MovePhotosDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(MovePhotosDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
