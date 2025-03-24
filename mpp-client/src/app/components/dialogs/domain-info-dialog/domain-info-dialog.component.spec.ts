import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DomainInfoDialogComponent } from './domain-info-dialog.component';

describe('DomainInfoDialogComponent', () => {
  let component: DomainInfoDialogComponent;
  let fixture: ComponentFixture<DomainInfoDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DomainInfoDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DomainInfoDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
