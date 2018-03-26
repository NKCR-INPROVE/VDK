import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse, HttpHeaderResponse } from '@angular/common/http';

// services
import { DkService } from '../service/dk.service';
import { ResultService } from '../service/result.service';

// property type
import { Dk, Result } from "../properties";
import { AppState } from "../app.state";
import { SolrService } from '../service/solr.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})

export class HomeComponent implements OnInit {
  nologo: string = "assets/img/nologo.png";
  dk: Dk[];
  results: Result[];
  toogleResults: boolean = false;


  errorMessage: string;
  errorStatus: string;

  constructor(private dkService: DkService,
              private resultService: ResultService,
              private solrService: SolrService,
              public state: AppState) { }

              
  ngOnInit() {
  }
  
  // get digital library json
  getDk(): void {
    this.dkService.getDk()
    .subscribe(
      dk => this.dk = dk, 
      (err: HttpErrorResponse) => {
        this.solrService.errorHandler(err);
      }
    );
  }
  
  // get results json
  getResult(): void {
    this.resultService.getResult()
    .subscribe(
      results => this.results = results,
      (err: HttpErrorResponse) => {
        this.solrService.errorHandler(err);
      }
    );
    this.toogleResults = !this.toogleResults;
  }

}
