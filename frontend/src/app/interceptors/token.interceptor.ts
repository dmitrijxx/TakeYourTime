import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    private noAuthUrls = [
        'api-feiertage.de'
    ];

    constructor(private readonly authService: AuthService) { }

    intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        if (this.noAuthUrls.some(url => request.url.includes(url))) {
            return next.handle(request);
        }

        const token = this.authService.isAuthenticatedReturnsToken();

        if (token && token !== '') {
            request = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`
                }
            });
        }

        return next.handle(request);
    }
}