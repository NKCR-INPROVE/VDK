import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';

// property type
import { Solr } from "./properties";

@Injectable()
export class AppState {
  public q: string;
  public results: Solr[];
  
  public errorMessage: string;
  public errorStatus: string;
}
