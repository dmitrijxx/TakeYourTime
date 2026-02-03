import { HttpClient } from '@angular/common/http';
import { Injectable, isDevMode } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(private http: HttpClient) { }

  private constructUrl(path: string): string {
    if (path.startsWith('/')) {
      path = path.slice(1);
    }

    if (isDevMode()) {
      return "http://localhost:8080/api/v1/" + path;
    }

    return "/api/v1/" + path;
  }

  public post(path: string, body?: any, options?: any): Observable<any> {
    return this.http.post(this.constructUrl(path), body, options);
  }

  public get(path: string, options?: any): Observable<any> {
    return this.http.get(this.constructUrl(path), options);
  }

  public delete(path: string, options?: any): Observable<any> {
    return this.http.delete(this.constructUrl(path), options);
  }
}
