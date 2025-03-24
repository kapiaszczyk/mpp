import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NoAlbumsCardComponent } from './no-albums-card.component';

describe('NoAlbumsCardComponent', () => {
  let component: NoAlbumsCardComponent;
  let fixture: ComponentFixture<NoAlbumsCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NoAlbumsCardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NoAlbumsCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
