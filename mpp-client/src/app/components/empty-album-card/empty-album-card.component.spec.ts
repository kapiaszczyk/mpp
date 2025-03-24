import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmptyAlbumCardComponent } from './empty-album-card.component';

describe('EmptyAlbumCardComponent', () => {
  let component: EmptyAlbumCardComponent;
  let fixture: ComponentFixture<EmptyAlbumCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmptyAlbumCardComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(EmptyAlbumCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
