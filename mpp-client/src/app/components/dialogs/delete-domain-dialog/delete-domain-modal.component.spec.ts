import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteDomainModalComponent } from './delete-domain-modal.component';

describe('DeleteDomainModalComponent', () => {
  let component: DeleteDomainModalComponent;
  let fixture: ComponentFixture<DeleteDomainModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteDomainModalComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DeleteDomainModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
