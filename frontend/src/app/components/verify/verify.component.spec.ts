import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VerifyComponent } from './verify.component';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router } from '@angular/router';
import { describe, it, expect, beforeEach, vi } from 'vitest';

describe('VerifyComponent', () => {
  let component: VerifyComponent;
  let fixture: ComponentFixture<VerifyComponent>;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        VerifyComponent,
        RouterTestingModule,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        MatCardModule,
        MatProgressSpinnerModule
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: (key: string) => (key === 'token' ? 'test-token' : null)
              }
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(VerifyComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to success on successful verification', () => {
    fixture.detectChanges(); // Trigger ngOnInit
    const req = httpMock.expectOne('/api/auth/verify?token=test-token');
    req.flush({});
    expect(router.navigate).toHaveBeenCalledWith(['/verify/success']);
  });

  it('should navigate to error on failed verification', () => {
    fixture.detectChanges();
    const req = httpMock.expectOne('/api/auth/verify?token=test-token');
    req.flush({ reason: 'expired', email: 't@e.com' }, { status: 400, statusText: 'Bad Request' });
    expect(router.navigate).toHaveBeenCalledWith(['/verify/error'], expect.any(Object));
  });
});
