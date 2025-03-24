import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShareAlbumButtonComponent } from './share-album-button.component';

describe('ShareAlbumButtonComponent', () => {
  let component: ShareAlbumButtonComponent;
  let fixture: ComponentFixture<ShareAlbumButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShareAlbumButtonComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ShareAlbumButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
