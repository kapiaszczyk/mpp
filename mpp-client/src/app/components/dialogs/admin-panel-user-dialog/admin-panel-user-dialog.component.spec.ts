import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminPanelUserModalComponent } from './admin-panel-user-dialog.component';

describe('AdminPanelUserModalComponent', () => {
  let component: AdminPanelUserModalComponent;
  let fixture: ComponentFixture<AdminPanelUserModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminPanelUserModalComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminPanelUserModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
