import { Component, OnInit } from '@angular/core';

import { AppState } from '../app.state';

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.scss']
})
export class ResultsComponent implements OnInit {

  constructor(public state: AppState) { }

  ngOnInit() {
  }

}
