import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResetPasswordComponent } from './reset-password.component';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { AuthService } from '../../services/auth.service';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let fixture: ComponentFixture<ResetPasswordComponent>;
  let authServiceSpy: any;

  beforeEach(async () => {
    authServiceSpy = {
      resetPassword: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [
        ResetPasswordComponent,
        ReactiveFormsModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatProgressBarModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: (key: string) => (key === 'token' ? 'valid-token' : null)
              }
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ResetPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should validate password strength', () => {
    const passwordControl = component.resetForm.get('password');
    passwordControl?.setValue('weak');
    expect(passwordControl?.hasError('strength')).toBe(true);

    passwordControl?.setValue('Strong123!');
    expect(passwordControl?.hasError('strength')).toBe(false);
  });

  it('should validate password match', () => {
    component.resetForm.patchValue({
      password: 'Strong123!',
      confirmPassword: 'Different123!'
    });
    expect(component.resetForm.hasError('mismatch')).toBe(true);

    component.resetForm.patchValue({
      confirmPassword: 'Strong123!'
    });
    expect(component.resetForm.hasError('mismatch')).toBe(false);
  });

  it('should submit successfully', () => {
    authServiceSpy.resetPassword.mockReturnValue(of(null));
    component.resetForm.patchValue({
      password: 'Strong123!',
      confirmPassword: 'Strong123!'
    });
    component.onSubmit();
    expect(authServiceSpy.resetPassword).toHaveBeenCalled();
    expect(component.success).toBe(true);
  });

  it('should handle submission error', () => {
    authServiceSpy.resetPassword.mockReturnValue(throwError(() => ({ error: { error: 'expired' } })));
    component.resetForm.patchValue({
      password: 'Strong123!',
      confirmPassword: 'Strong123!'
    });
    component.onSubmit();
    expect(component.error).toContain('expired');
  });
});
