import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  OnInit,
  Output, SimpleChanges,
  ViewChild
} from "@angular/core";



enum ThumbEvent {
  THUMB_PRESSED,
  THUMB_RELEASED
}

enum Thumb {
  START, END
}

@Component({
  selector: '[app-circular-range-slider]',
  templateUrl: './circular-range-slider.component.html',
  styleUrls: ['./circular-range-slider.component.scss']
})
export class CircularRangeSliderComponent implements OnInit, OnChanges {
  _startAngle = Math.PI / 3;
  _endAngle = Math.PI / 2;

  _borderStartAngle = 0.0;
  _borderEndAngle = Math.PI * 2;
  @Input() borderColor: string = "#FFFFFF";
  @Input() borderThickness = 2.0;
  @Input() offsetAngle = Math.PI ;


  @Input() thumbSize = 5.0;
  @Input() drawThumbSize = 5.0;
  @Input() startThumbColor: string = "#FFFFFF";
  @Input() endThumbColor: string = "#FFFFFF";


  @Input() arcColor: string = "#FFFFFF";
  @Input() arcThickness = 2;

  @Output() startSliderMoved = new EventEmitter();
  @Output() endSliderMoved = new EventEmitter();
  @Output() startSliderEvent = new EventEmitter();
  @Output() endSliderEvent = new EventEmitter();

  @ViewChild("startThumb") thumbStart;
  @ViewChild("endThumb") thumbEnd;
  @ViewChild("borderArc") borderArc;
  @ViewChild("selectedArc") selectedArc;
  @ViewChild("canvas") canvas;

  public centerX = 0;
  public centerY = 0;
  public circleRadius = 0;
  public thumbStartX = 0;
  public thumbStartY = 0;
  public thumbEndX = 0;
  public thumbEndY = 0;
  constructor(private el: ElementRef) { }

  ngOnInit() {
    document.documentElement.onmousemove = this.onMouseMove.bind(this);
    document.documentElement.onmouseup = this.onMouseUp.bind(this);
    this.el.nativeElement.onmousedown = this.onMouseDown.bind(this);
    window.onresize = (e) =>  {
      this.centerX = this.el.nativeElement.clientWidth / 2;
      this.centerY = this.el.nativeElement.clientWidth / 2;
      this.canvas.nativeElement.style.width = this.el.nativeElement.clientWidth;
      this.canvas.nativeElement.style.height = this.el.nativeElement.clientWidth;
      this.canvas.nativeElement.setAttribute('width', this.canvas.nativeElement.style.width);
      this.canvas.nativeElement.setAttribute('height', this.canvas.nativeElement.style.height);
      this.circleRadius = this.el.nativeElement.clientWidth / 2 - this.drawThumbSize / 2;
      this.updateDraw()
    };
    document.addEventListener("DOMContentLoaded", (e) => {
      this.centerX = this.el.nativeElement.clientWidth / 2;
      this.centerY = this.el.nativeElement.clientWidth / 2;
      this.canvas.nativeElement.style.width = this.el.nativeElement.clientWidth;
      this.canvas.nativeElement.style.height = this.el.nativeElement.clientWidth;
      this.canvas.nativeElement.setAttribute('width', this.canvas.nativeElement.style.width);
      this.canvas.nativeElement.setAttribute('height', this.canvas.nativeElement.style.height);
      this.circleRadius = this.el.nativeElement.clientWidth / 2 - this.drawThumbSize / 2;
      this.updateDraw()
    });

    this.centerX = this.el.nativeElement.clientWidth / 2;
    this.centerY = this.el.nativeElement.clientWidth / 2;
    this.canvas.nativeElement.style.width = this.el.nativeElement.clientWidth;
    this.canvas.nativeElement.style.height = this.el.nativeElement.clientWidth;
    this.canvas.nativeElement.setAttribute('width', this.canvas.nativeElement.style.width);
    this.canvas.nativeElement.setAttribute('height', this.canvas.nativeElement.style.height);
    this.circleRadius = this.el.nativeElement.clientWidth / 2 - this.drawThumbSize / 2;
    this.updateDraw()
  }

  get borderStartAngle() {
    return this._borderStartAngle;
  }

  @Input()
  set borderStartAngle(value) {
    this._borderStartAngle = value > this.borderStartAngle ? value : this.borderStartAngle;
  }

  get borderEndAngle() {
    return this._borderEndAngle;
  }

  @Input()
  set borderEndAngle(value) {
    this._borderEndAngle = value > this.borderEndAngle ? value : this.borderEndAngle;
  }

  get startAngle() {
    return this._startAngle;
  }

  @Input()
  set startAngle(value) {
    this._startAngle = value < 0 ? 0 : value;
  }

  get endAngle() {
    return this._endAngle;
  }

  @Input()
  set endAngle(value) {
    this._endAngle = value < 0 ? 0 : value;
  }

  updateDraw() {

    this.thumbStartX = this.centerX + this.circleRadius * Math.cos(this.startAngle) - this.drawThumbSize / 2;
    this.thumbStartY = this.centerY - this.circleRadius * Math.sin(this.startAngle) - this.drawThumbSize / 2;
    this.thumbEndX = this.centerX + this.circleRadius * Math.cos(this.endAngle) - this.drawThumbSize / 2;
    this.thumbEndY = this.centerY - this.circleRadius * Math.sin(this.endAngle) - this.drawThumbSize / 2;
    const ctx: CanvasRenderingContext2D = this.canvas.nativeElement.getContext("2d");
    ctx.clearRect(0, 0, this.canvas.nativeElement.width, this.canvas.nativeElement.height);
    ctx.rotate(this.offsetAngle);
    ctx.beginPath();
    ctx.lineWidth = this.borderThickness;
    ctx.strokeStyle = window.getComputedStyle(this.borderArc.nativeElement, null).getPropertyValue('background-color');
    console.log(this.borderStartAngle, this.borderEndAngle);
    ctx.arc(this.centerX,this.centerY, this.circleRadius,
            this.borderStartAngle, this.borderEndAngle);
    ctx.stroke();
    ctx.beginPath();
    ctx.lineWidth = this.arcThickness;
    ctx.strokeStyle = window.getComputedStyle(this.selectedArc.nativeElement, null).getPropertyValue('background-color');
    ctx.arc(this.centerX, this.centerY, this.circleRadius,
            this.startAngle, this.endAngle);
    ctx.stroke();
    console.log(this.selectedArc.nativeElement)
    ctx.beginPath();
    ctx.lineWidth = 0;
    ctx.strokeStyle = window.getComputedStyle(this.thumbStart.nativeElement, null).getPropertyValue('background-color');
    ctx.arc(this.thumbStartX + this.drawThumbSize / 2, this.thumbStartY + this.drawThumbSize / 2, this.drawThumbSize / 2,
            0, 2 * Math.PI);
    ctx.fill();

    ctx.beginPath();
    ctx.lineWidth = 0;
    ctx.strokeStyle = window.getComputedStyle(this.thumbEnd.nativeElement, null).getPropertyValue('background-color');
    ctx.arc(this.thumbEndX + this.drawThumbSize / 2, this.thumbEndY + this.drawThumbSize / 2, this.drawThumbSize / 2,
      0, 2 * Math.PI);
    ctx.fill();
  }

  updateSliderState(touchX, touchY, thumb) {const rect = this.el.nativeElement.getBoundingClientRect();
    const centerX = this.centerX + rect.left;
    const centerY = this.centerY + rect.top;
    const distanceX = touchX - centerX;
    const distanceY = centerY - touchY;

    const c = Math.sqrt(Math.pow(distanceX, 2.0) + Math.pow(distanceY, 2.0));
    let angle = Math.acos(distanceX / c);
    if (distanceY < 0)
      angle = -angle;

    let perc = 0.0;
    if (thumb == Thumb.START) {
      this.startAngle = angle;
      if(this.startAngle > this.endAngle) {
        this.startAngle = this.borderStartAngle
      }
      perc = (this.startAngle - this.borderStartAngle) / (this.borderEndAngle - this.borderStartAngle)
    } else {
      this.endAngle = angle;
      if(this.startAngle > this.endAngle) {
        this.endAngle = this.borderEndAngle
      }
      perc = (this.endAngle - this.borderStartAngle) / (this.borderEndAngle - this.borderStartAngle)
    }
    this.updateDraw();


    if (thumb == Thumb.START) {
      this.startSliderMoved.next(perc);
    } else {
      this.endSliderMoved.next(perc);
    }
  }

  private isStartThumbSelected = false;
  private isEndThumbSelected = false;

  private onMouseDown(event: MouseEvent) {
    event.preventDefault();
    const angle = this.offsetAngle;

    const rect = this.el.nativeElement.getBoundingClientRect();
    const centerX = this.centerX + rect.left;
    const centerY = this.centerY + rect.top;

    const x1 = event.clientX - centerX;
    const y1 = event.clientY - centerY;

    const x2 = x1 * Math.cos(angle) - y1 * Math.sin(angle);
    const y2 = x1 * Math.sin(angle) + y1 * Math.cos(angle);

    const x = x2 + centerX;
    const y = y2 + centerY;

    const thumbStartX = rect.left + this.thumbStartX;
    const thumbStartY = rect.top + this.thumbStartY;
    const thumbEndX = rect.left + this.thumbEndX;
    const thumbEndY = rect.top + this.thumbEndY;

    const isThumbStartPressed = (x < thumbStartX + this.thumbSize
                                 && x > thumbStartX - this.thumbSize
                                 && y < thumbStartY + this.thumbSize
                                 && y > thumbStartY - this.thumbSize);

    const isThumbEndPressed = (x < thumbEndX + this.thumbSize
                               && x > thumbEndX - this.thumbSize
                               && y < thumbEndY + this.thumbSize
                               && y > thumbEndY - this.thumbSize);

    console.log("Start: sadasd ");
    if (isThumbStartPressed) {
      event.preventDefault();
      this.isStartThumbSelected = true;
      this.updateSliderState(x, y, Thumb.START);
      this.startSliderEvent.next(ThumbEvent.THUMB_PRESSED);

    } else if (isThumbEndPressed) {
      event.preventDefault();
      this.isEndThumbSelected = true;
      this.updateSliderState(x, y, Thumb.END);
      this.endSliderEvent.next(ThumbEvent.THUMB_PRESSED);
    }
  }

  private onMouseMove(event: MouseEvent) {
    if (this.isStartThumbSelected) {
      event.preventDefault();
      console.log("Move: sadasd ");
      const angle = this.offsetAngle;

      const rect = this.el.nativeElement.getBoundingClientRect();

      const centerX = this.centerX + rect.left;
      const centerY = this.centerY + rect.top;

      const x1 = event.clientX - centerX;
      const y1 = event.clientY - centerY;

      const x2 = x1 * Math.cos(angle) - y1 * Math.sin(angle);
      const y2 = x1 * Math.sin(angle) + y1 * Math.cos(angle);

      const x = x2 + centerX;
      const y = y2 + centerY;

      this.updateSliderState(x, y, Thumb.START)
    } else if (this.isEndThumbSelected) {
      event.preventDefault();
      const angle = this.offsetAngle;

      const rect = this.el.nativeElement.getBoundingClientRect();

      const centerX = this.centerX + rect.left;
      const centerY = this.centerY + rect.top;

      const x1 = event.clientX - centerX;
      const y1 = event.clientY - centerY;

      const x2 = x1 * Math.cos(angle) - y1 * Math.sin(angle);
      const y2 = x1 * Math.sin(angle) + y1 * Math.cos(angle);

      const x = x2 + centerX;
      const y = y2 + centerY;

      this.updateSliderState(x, y, Thumb.END)
    }
  }

  private onMouseUp(event: MouseEvent) {
    if (this.isStartThumbSelected) {
      console.log("End: sadasd ");
      this.startSliderEvent.next(ThumbEvent.THUMB_RELEASED);
    } else if (this.isEndThumbSelected) {
      this.endSliderEvent.next(ThumbEvent.THUMB_RELEASED);
    }

    this.isStartThumbSelected = false;
    this.isEndThumbSelected = false;
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.updateDraw()
  }

}
