import { Component, OnInit, Input } from '@angular/core';
import { Result } from '../../models/result';

@Component({
  selector: 'app-results-item',
  templateUrl: './results-item.component.html',
  styleUrls: ['./results-item.component.scss']
})
export class ResultsItemComponent implements OnInit {
  @Input() item: [];
  
  constructor() { }

  ngOnInit() {
  }

}
