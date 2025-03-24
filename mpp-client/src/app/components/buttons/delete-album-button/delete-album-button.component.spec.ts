import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteAlbumButtonComponent } from './delete-album-button.component';

describe('DeleteAlbumButtonComponent', () => {
  let component: DeleteAlbumButtonComponent;
  let fixture: ComponentFixture<DeleteAlbumButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteAlbumButtonComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DeleteAlbumButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
