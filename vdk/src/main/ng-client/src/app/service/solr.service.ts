import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from "rxjs/Observable";
import { Router, ActivatedRoute, Params, NavigationExtras } from '@angular/router';

import { Result } from '../models/result';
import { Facet } from '../models/facet';

import { AppState } from '../app.state';

@Injectable()
export class SolrService {

  private solrUrl: string = '/solr/rdcz/select';

  constructor(private http: HttpClient,
              private router: Router,
              private activatedRoute: ActivatedRoute,
              public state: AppState) { }

  // get solr data
  getSolr(params): Observable<Result[]> {
    if(this.state.q !='') {
      return this.http.get<Result[]>(this.solrUrl, {params: params});
    } else {
      return this.http.get<Result[]>(this.solrUrl + '?q=*.*');
    }
  }

  // search solr
  searchSolr(params) {
    this.getSolr(params)
    .subscribe(
      (solr) => this.state.result = solr["response"],
      (err: HttpErrorResponse) => {
        this.errorHandler(err);
      }
    );
  }

  // error handler
  errorHandler(error: any): void {
    if (status === '404') {
      console.log(error);
      return this.state.errorMessage = error.message, this.state.errorStatus = error.status;
    } else {
      console.log(error);
      return this.state.errorMessage = error.message, this.state.errorStatus = error.status;
    }
  }

  // console writer
  consoleWriter(params): void {
    this.getSolr(params)
    .subscribe(
      data => {
        console.log(data);
      }
    );
  }

  // do url params
  doUrlParams(): NavigationExtras  {
    let params = {};
    params['q'] = this.state.q;
    params['facet'] = this.state.facet;
    params['rows'] = this.state.rows;
    params["facet.field"] = [];
    for(let field of this.state.facets) {
      params["facet.field"].push(field);
    }
    return params;
  }

  // set url params
  setUrlParams(params): void {
    if (this.router.url == '/home') {    
      //this.router.navigate(['/results'], { queryParams: {q: this.state.q} });
      this.router.navigate(['/results'], { queryParams: params });
      //console.log(this.router.url);
    } else {
      this.router.navigate([], { queryParams: params });
      //this.router.navigate([params]);
      //console.log(this.router.url);
    }
  }

  // get params
  getUrlParams() {
    this.activatedRoute.queryParams.subscribe((params: Params) => {
      if (params['q'] && params['q'] != '') {
        this.state.q = params['q'];
      }
      if (params.hasOwnProperty('rows')) {
        this.state.rows = params['rows'];
      }
      if (params.hasOwnProperty('facet')) {
        this.state.facet = params['facet'];
      }
      if (params.hasOwnProperty('facet.field')) {
        this.state.facets = params['facet.field'];
      }
      this.state.urlParams = params;
    });
  }
}
