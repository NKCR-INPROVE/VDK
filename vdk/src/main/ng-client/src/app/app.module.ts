import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpModule } from "@angular/http";
import { HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';

// others
import {MaterializeModule} from 'ng2-materialize';

// services
import { DkService } from './service/dk.service';
import { ResultService } from './service/result.service';
import { SolrService } from './service/solr.service';
import { AppState } from './app.state';

// components
import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { SearchBarComponent } from './search-bar/search-bar.component';
import { ResultsComponent } from './results/results.component';


@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    SearchBarComponent,
    ResultsComponent
  ],
  imports: [
    BrowserModule,
    HttpModule,
    HttpClientModule,
    RouterModule.forRoot([
      { path: 'home', component: HomeComponent },
      { path: '', redirectTo: '/home', pathMatch: 'full' }
    ]),
    MaterializeModule.forRoot()
  ],
  providers: [DkService, ResultService, AppState, SolrService],
  bootstrap: [AppComponent]
})
export class AppModule { }
