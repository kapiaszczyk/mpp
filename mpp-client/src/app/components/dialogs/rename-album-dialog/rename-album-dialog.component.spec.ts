import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RenameAlbumDialogComponent } from './rename-album-dialog.component';

describe('RenameAlbumDialogComponent', () => {
  let component: RenameAlbumDialogComponent;
  let fixture: ComponentFixture<RenameAlbumDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RenameAlbumDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RenameAlbumDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
