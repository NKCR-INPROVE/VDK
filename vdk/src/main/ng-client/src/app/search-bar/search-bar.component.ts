import { Component, OnInit } from '@angular/core';

import { SolrService } from '../service/solr.service';
import { AppState } from '../app.state';


@Component({
  selector: 'app-search-bar',
  templateUrl: './search-bar.component.html',
  styleUrls: ['./search-bar.component.scss']
})
export class SearchBarComponent implements OnInit {

  constructor(private solrService: SolrService,
              private state: AppState) { }

  ngOnInit() {
    this.solrService.getUrlParams();

    /* let sparams = this.solrService.doUrlParams();
    this.solrService.searchSolr(sparams);
    this.solrService.consoleWriter(sparams); */
  }

  // search in solr data
  search(): void {
    let sparams = this.solrService.doUrlParams();
    this.solrService.setUrlParams(sparams);
    this.solrService.getUrlParams();
    this.solrService.searchSolr(sparams);
  }
}
