import { Component, OnInit } from '@angular/core';

import { SolrService } from '../service/solr.service';
import { AppState } from '../app.state';

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.scss']
})
export class ResultsComponent implements OnInit {

  constructor(public state: AppState,
              private solrService: SolrService) { }

  ngOnInit() {
    let sparams = this.solrService.doUrlParams();
    this.solrService.searchSolr(sparams);
  }
}
