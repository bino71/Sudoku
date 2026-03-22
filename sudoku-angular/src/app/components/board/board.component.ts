import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgClass } from '@angular/common';
import { BoardDto } from '../../models/board.model';
import { CellComponent } from '../cell/cell.component';

@Component({
  selector: 'app-board',
  standalone: true,
  imports: [NgClass, CellComponent],
  templateUrl: './board.component.html',
  styleUrl: './board.component.scss'
})
export class BoardComponent {
  @Input() board: BoardDto | null = null;
  @Input() selectedCell: { row: number; col: number } | null = null;
  @Input() hintedCell: { row: number; col: number } | null = null;

  @Output() cellSelected = new EventEmitter<{ row: number; col: number }>();
  @Output() valueEntered = new EventEmitter<{ row: number; col: number; value: number }>();

  rows = [0, 1, 2, 3, 4, 5, 6, 7, 8];
  cols = [0, 1, 2, 3, 4, 5, 6, 7, 8];

  isSelected(row: number, col: number): boolean {
    return this.selectedCell?.row === row && this.selectedCell?.col === col;
  }

  isHinted(row: number, col: number): boolean {
    return this.hintedCell?.row === row && this.hintedCell?.col === col;
  }

  getCellClasses(row: number, col: number): Record<string, boolean> {
    return {
      'box-border-right': col === 2 || col === 5,
      'box-border-bottom': row === 2 || row === 5
    };
  }

  onCellClick(row: number, col: number): void {
    this.cellSelected.emit({ row, col });
  }

  onDigitKey(row: number, col: number, digit: number): void {
    if (this.board && !this.board.given[row][col]) {
      this.valueEntered.emit({ row, col, value: digit });
    }
  }

  onClearKey(row: number, col: number): void {
    if (this.board && !this.board.given[row][col]) {
      this.valueEntered.emit({ row, col, value: 0 });
    }
  }
}
