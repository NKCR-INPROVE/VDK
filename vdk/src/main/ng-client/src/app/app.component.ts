import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'app';
  view: boolean;
  rows = [1, 2, 3, 4, 5, 6, 7, 8, 9];
  text: string = "Vestibulum erat nullaVestibulum erat nulla";
  toggle(){
	  this.view = !this.view;
	  this.text += this.text;
  }
}
