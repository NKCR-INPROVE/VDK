import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

// services
import { DkService } from '../service/dk.service';
import { ResultService } from '../service/result.service';

// property type
import {Dk, Result} from "../properties";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})

export class HomeComponent implements OnInit {
  nologo: string = "assets/img/nologo.png";
  dk: Dk[];
  results: Result[];
  errorMessage: string;
  errorStatus: string;
  toogleResults: boolean = false;

  constructor(private dkService: DkService,
              private resultService: ResultService) { }
 
  ngOnInit(): void {
    this.getDk();
    //this.getResult();
  }
  
  // get digital library json
  getDk(): void {
    this.dkService.getDk()
    .subscribe(
      dk => this.dk = dk, 
      (err: HttpErrorResponse) => {
        this.errorHandler(err);
      }
    );
  }
  
  // get results json
  getResult(): void {
    this.resultService.getResult()
    .subscribe(
      results => this.results = results,
      (err: HttpErrorResponse) => {
        this.errorHandler(err);
      }
    );
    this.toogleResults = !this.toogleResults;
  }
  
  // error handler
  errorHandler(error: any): void {
    if (status === '404') {
      console.log(error);
      return this.errorMessage = error.message, this.errorStatus = error.status;
    } else {
      console.log(error);
      return this.errorMessage = error.message, this.errorStatus = error.status;
    }
  }

}
