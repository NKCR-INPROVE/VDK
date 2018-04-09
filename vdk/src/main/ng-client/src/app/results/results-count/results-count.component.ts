import { Component, OnInit } from '@angular/core';

import { AppState } from '../../app.state';

@Component({
  selector: 'app-results-count',
  templateUrl: './results-count.component.html',
  styleUrls: ['./results-count.component.scss']
})
export class ResultsCountComponent implements OnInit {

  constructor(public state: AppState) { }

  ngOnInit() {
  }

}
