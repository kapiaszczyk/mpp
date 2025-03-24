import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PhotoTagsComponent } from './photo-tags.component';

describe('PhotoTagsComponent', () => {
  let component: PhotoTagsComponent;
  let fixture: ComponentFixture<PhotoTagsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PhotoTagsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PhotoTagsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
