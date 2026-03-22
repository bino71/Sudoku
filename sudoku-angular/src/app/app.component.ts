import { Component, inject, OnInit } from '@angular/core';
import { GameService } from './services/game.service';
import { BoardDto } from './models/board.model';
import { BoardComponent } from './components/board/board.component';
import { ControlBarComponent } from './components/control-bar/control-bar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [BoardComponent, ControlBarComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  private readonly gameService = inject(GameService);

  board: BoardDto | null = null;
  selectedCell: { row: number; col: number } | null = null;
  hintedCell: { row: number; col: number } | null = null;
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.gameService.getState().subscribe({
      next: (board) => {
        if (board) {
          this.board = board;
        } else {
          this.startNewGame('EASY');
        }
      },
      error: (err) => {
        if (err.status === 204) {
          this.startNewGame('EASY');
        } else {
          this.startNewGame('EASY');
        }
      }
    });
  }

  startNewGame(difficulty: string): void {
    this.gameService.newGame(difficulty).subscribe({
      next: (board) => {
        this.board = board;
        this.selectedCell = null;
        this.hintedCell = null;
        this.errorMessage = null;
      },
      error: (err) => {
        this.errorMessage = 'Failed to start a new game. Is the server running?';
        console.error('newGame error', err);
      }
    });
  }

  onCellSelected(cell: { row: number; col: number }): void {
    this.selectedCell = cell;
    this.hintedCell = null;
  }

  onValueEntered(event: { row: number; col: number; value: number }): void {
    this.gameService.enterValue(event.row, event.col, event.value).subscribe({
      next: (board) => {
        this.board = board;
        this.selectedCell = { row: event.row, col: event.col };
      },
      error: (err) => {
        console.error('enterValue error', err);
      }
    });
  }

  onHint(): void {
    this.gameService.hint().subscribe({
      next: (response) => {
        if (response.board) {
          this.board = response.board;
        }
        if (response.row !== undefined && response.col !== undefined) {
          this.hintedCell = { row: response.row, col: response.col };
          this.selectedCell = null;
          // Clear hint highlight after animation completes
          setTimeout(() => {
            this.hintedCell = null;
          }, 2000);
        }
        if (response.message) {
          console.info('Hint:', response.message);
        }
      },
      error: (err) => {
        console.error('hint error', err);
      }
    });
  }

  onSolve(): void {
    this.gameService.solve().subscribe({
      next: (board) => {
        this.board = board;
        this.selectedCell = null;
        this.hintedCell = null;
      },
      error: (err) => {
        console.error('solve error', err);
      }
    });
  }

  onReset(): void {
    this.gameService.reset().subscribe({
      next: (board) => {
        this.board = board;
        this.selectedCell = null;
        this.hintedCell = null;
        this.errorMessage = null;
      },
      error: (err) => {
        console.error('reset error', err);
      }
    });
  }

  onNewGame(difficulty: string): void {
    this.startNewGame(difficulty);
  }

  dismissCompletion(): void {
    // Keep board visible but clear overlay by navigating to new game
  }
}
