import { Component } from '@angular/core';
import { BreakpointObserver, Breakpoints, BreakpointState } from '@angular/cdk/layout';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss'],

})
export class ToolbarComponent {
  isHandset: Observable<BreakpointState> = this.breakpointObserver.observe(Breakpoints.Handset);
  tabs = [
    {path: "", label: "PAGES.HOME.HOME"},
    {path: "secondary/", label: "PAGES.HOME.TEST2"}
  ]
  constructor(private breakpointObserver: BreakpointObserver) {}
}
