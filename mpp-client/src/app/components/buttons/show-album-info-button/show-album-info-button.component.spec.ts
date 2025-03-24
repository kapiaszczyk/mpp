import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShowAlbumInfoButtonComponent } from './show-album-info-button.component';

describe('ShowAlbumInfoButtonComponent', () => {
  let component: ShowAlbumInfoButtonComponent;
  let fixture: ComponentFixture<ShowAlbumInfoButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShowAlbumInfoButtonComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ShowAlbumInfoButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
