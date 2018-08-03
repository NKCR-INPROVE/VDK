import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';

// models
import { Result } from "./models/result";
import { Facet } from "./models/facet";

@Injectable()
export class AppState {
  // params
  public q: string;
  rows: number = 15;
  facet: boolean = false;
  facets: any = [
    "stav",
    "druhdokumentu"
  ];
  urlParams: {};

  result: Result[];
  
  public errorMessage: string;
  public errorStatus: string;

  clearParams() {
    this.q = '';
  }


}

// ?facet.field=stav&facet.field=druhdokumentu&facet=on&fq=stav:progress&indent=on&q=hrad&wt=json
