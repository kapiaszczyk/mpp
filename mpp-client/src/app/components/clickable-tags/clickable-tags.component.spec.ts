import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClickableTagsComponent } from './clickable-tags.component';

describe('ClickableTagsComponent', () => {
  let component: ClickableTagsComponent;
  let fixture: ComponentFixture<ClickableTagsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClickableTagsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClickableTagsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
