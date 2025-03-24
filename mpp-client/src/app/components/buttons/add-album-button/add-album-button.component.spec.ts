import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddAlbumButtonComponent } from './add-album-button.component';

describe('AddAlbumButtonComponent', () => {
  let component: AddAlbumButtonComponent;
  let fixture: ComponentFixture<AddAlbumButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddAlbumButtonComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddAlbumButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
