import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RenameAlbumButtonComponent } from './rename-album-button.component';

describe('RenameAlbumButtonComponent', () => {
  let component: RenameAlbumButtonComponent;
  let fixture: ComponentFixture<RenameAlbumButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RenameAlbumButtonComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RenameAlbumButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
