import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RemoveAlbumCoverButtonComponent } from './remove-album-cover-button.component';

describe('RemoveAlbumCoverButtonComponent', () => {
  let component: RemoveAlbumCoverButtonComponent;
  let fixture: ComponentFixture<RemoveAlbumCoverButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RemoveAlbumCoverButtonComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RemoveAlbumCoverButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
