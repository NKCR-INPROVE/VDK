import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from "rxjs/Observable";
import { Solr } from '../properties'; // property type

import { AppState } from '../app.state';

@Injectable()
export class SolrService {

  private solrUrl: string = '/solr/rdcz/select';

  constructor(private http: HttpClient,
              public state: AppState) { }

  // get solr data
  getSolr(): Observable<Solr[]> {
    if(this.state.q !='') {
      return this.http.get<Solr[]>(this.solrUrl + '?q=' + this.state.q);
    } else {
      return this.http.get<Solr[]>(this.solrUrl + '?q=*.*');
    }
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
   consoleWriter(): void {
    this.getSolr()
    .subscribe(
      data => {
        console.log(data);
      }
    );
  }
}
