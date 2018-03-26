import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpErrorResponse, HttpHeaderResponse } from '@angular/common/http';

// services
import { SolrService } from '../service/solr.service';
import { AppState } from '../app.state';

// property type
import { Solr } from "../properties";

@Component({
  selector: 'app-search-bar',
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.scss']
})
export class SearchBarComponent implements OnInit {

  errorMessage: string;
  errorStatus: string;

  constructor(private solrService: SolrService,
              public state: AppState) { }

  ngOnInit() {
  }

  // search in solr data
  search(): void {
    this.solrService.getSolr()
    .subscribe(
      solr => this.state.results = solr["response"],
      (err: HttpErrorResponse) => {
        this.solrService.errorHandler(err);
      }
    );
    this.solrService.consoleWriter();
  }

}
