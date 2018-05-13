// import { ToolbarComponent } from './components/toolbar/toolbar.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { MainUiComponent } from "./components/main-ui/main-ui.component";
import { SecondaryUiComponent } from "./components/secondary-ui/secondary-ui.component";

const routes: Routes = [
  {
      path: '',
      component: MainUiComponent
  },
  {
    path: 'secondary',
    component: SecondaryUiComponent
  }
];

@NgModule({
    imports: [RouterModule.forRoot(routes, {useHash: true})],
    exports: [RouterModule]
})
export class AppRoutingModule { }
