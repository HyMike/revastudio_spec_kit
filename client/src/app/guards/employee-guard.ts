import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth-service';
import { JwtStorage } from '../services/jwt-storage';

export const employeeGuard: CanActivateFn = () => {
  const jwtStorage = inject(JwtStorage);
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!jwtStorage.getToken()) {
    return router.createUrlTree(['/login']);
  }

  if (authService.getRole() === 'EMPLOYEE') {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
