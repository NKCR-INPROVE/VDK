import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpModule } from "@angular/http";
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

// others
import { MaterializeModule } from 'ng2-materialize';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';

// services
import { SolrService } from './service/solr.service';
import { AppState } from './app.state';

// components
import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { SearchBarComponent } from './search-bar/search-bar.component';
import { ResultsComponent } from './results/results.component';
import { NavbarComponent } from './navbar/navbar.component';
import { FooterComponent } from './footer/footer.component';
import { ResultsCountComponent } from './results/results-count/results-count.component';
import { ResultsPaginationComponent } from './results/results-pagination/results-pagination.component';
import { ResultsFacetsComponent } from './results/results-facets/results-facets.component';
import { ResultsFacetsUsedComponent } from './results/results-facets-used/results-facets-used.component';
import { ResultsItemComponent } from './results/results-item/results-item.component';

export function createTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}


@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    SearchBarComponent,
    ResultsComponent,
    NavbarComponent,
    FooterComponent,
    ResultsCountComponent,
    ResultsPaginationComponent,
    ResultsFacetsComponent,
    ResultsFacetsUsedComponent,
    ResultsItemComponent
  ],
  imports: [
    BrowserModule,
    HttpModule,
    HttpClientModule,
    RouterModule.forRoot([
      { path: 'results', component: ResultsComponent },
      { path: 'home', component: HomeComponent },
      { path: '', redirectTo: '/home', pathMatch: 'full' }
    ]),
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: createTranslateLoader,
        deps: [HttpClient]
      }
    }),
    MaterializeModule.forRoot()
  ],
  providers: [AppState, SolrService],
  bootstrap: [AppComponent]
})
export class AppModule { }
