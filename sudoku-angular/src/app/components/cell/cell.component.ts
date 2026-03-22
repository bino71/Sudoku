import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-cell',
  standalone: true,
  imports: [NgClass],
  templateUrl: './cell.component.html',
  styleUrl: './cell.component.scss'
})
export class CellComponent {
  @Input() value: number = 0;
  @Input() given: boolean = false;
  @Input() conflict: boolean = false;
  @Input() selected: boolean = false;
  @Input() hinted: boolean = false;

  @Output() cellClick = new EventEmitter<void>();
  @Output() digitKey = new EventEmitter<number>();
  @Output() clearKey = new EventEmitter<void>();

  get displayValue(): string {
    return this.value > 0 ? String(this.value) : '';
  }

  onKeyDown(event: KeyboardEvent): void {
    const key = event.key;
    if (key >= '1' && key <= '9') {
      event.preventDefault();
      this.digitKey.emit(Number(key));
    } else if (key === 'Backspace' || key === 'Delete' || key === '0') {
      event.preventDefault();
      this.clearKey.emit();
    }
  }

  onClick(): void {
    this.cellClick.emit();
  }
}
