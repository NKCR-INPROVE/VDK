import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from "rxjs/Observable";
import "rxjs/Rx";
import { Dk } from "../properties"; // property type

@Injectable()
export class DkService {
  
  private dkUrl = "/api/libraries.json";

  constructor(private http: HttpClient) { }
  
  getDk(): Observable<Dk[]> {
    return this.http.get<Dk[]>(this.dkUrl);
  }
  
  private handleError(error: Response) {
    return Observable.throw(error.statusText);
  }

}
