import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NoTagsCardComponent } from './no-tags-card.component';

describe('NoTagsCardComponent', () => {
  let component: NoTagsCardComponent;
  let fixture: ComponentFixture<NoTagsCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NoTagsCardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NoTagsCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
