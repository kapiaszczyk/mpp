import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AlbumCardsComponent } from './album-cards.component';

describe('AlbumCardsComponent', () => {
  let component: AlbumCardsComponent;
  let fixture: ComponentFixture<AlbumCardsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AlbumCardsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AlbumCardsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
