import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SharedDomainsGalleryComponent } from './shared-domains-gallery.component';

describe('SharedDomainsGalleryComponent', () => {
  let component: SharedDomainsGalleryComponent;
  let fixture: ComponentFixture<SharedDomainsGalleryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SharedDomainsGalleryComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SharedDomainsGalleryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
