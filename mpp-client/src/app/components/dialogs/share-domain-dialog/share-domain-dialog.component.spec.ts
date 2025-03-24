import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShareDomainDialogComponent } from './share-domain-dialog.component';

describe('ShareDomainDialogComponent', () => {
  let component: ShareDomainDialogComponent;
  let fixture: ComponentFixture<ShareDomainDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShareDomainDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ShareDomainDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
