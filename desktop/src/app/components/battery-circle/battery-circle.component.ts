import { AfterViewInit, Component, ElementRef, OnInit } from "@angular/core";

@Component({
  selector: '[app-battery-circle]',
  templateUrl: './battery-circle.component.html',
  styleUrls: ['./battery-circle.component.scss'],
  host: {
    class:"square",
  }
})
export class BatteryCircleComponent implements OnInit {
  public PI = Math.PI

  constructor(public el: ElementRef) { }

  ngOnInit() {
    console.log(this.el.nativeElement);

    this.el.nativeElement.parentElement.querySelectorAll(".square").forEach(it => {
      it.style.height = (it.clientWidth) + "px";
    });

    window.onresize = (e) =>  {
      this.el.nativeElement.parentElement.querySelectorAll(".square").forEach(it => {
        it.style.height = (it.clientWidth) + "px";
      })
    }
  }
}
