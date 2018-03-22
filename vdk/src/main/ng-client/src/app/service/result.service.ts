import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from "rxjs/Observable";
import "rxjs/Rx";
import { Result } from "../properties"; // property type

@Injectable()
export class ResultService {
  
  private resultUrl = "/assets/results.json";

  constructor(private http: HttpClient) { }

  getResult(): Observable<Result[]> {
    return this.http.get<Result[]>(this.resultUrl);
  }
  
  private handleError(error: Response) {
    return Observable.throw(error.statusText);
  }
}
