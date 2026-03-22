import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-control-bar',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './control-bar.component.html',
  styleUrl: './control-bar.component.scss'
})
export class ControlBarComponent implements OnInit, OnChanges, OnDestroy {
  @Input() difficulty: string = 'EASY';
  @Input() mistakeCount: number = 0;
  @Input() complete: boolean = false;

  @Output() newGame = new EventEmitter<string>();
  @Output() hint = new EventEmitter<void>();
  @Output() solve = new EventEmitter<void>();
  @Output() reset = new EventEmitter<void>();

  selectedDifficulty: string = 'EASY';
  difficulties = ['EASY', 'MEDIUM', 'HARD'];

  elapsedSeconds: number = 0;
  private intervalId: ReturnType<typeof setInterval> | null = null;

  ngOnInit(): void {
    this.selectedDifficulty = this.difficulty || 'EASY';
    if (!this.complete) {
      this.startTimer();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['difficulty'] && !changes['difficulty'].firstChange) {
      this.selectedDifficulty = this.difficulty;
    }
    if (changes['complete']) {
      if (this.complete) {
        this.stopTimer();
      } else {
        this.resetTimer();
        this.startTimer();
      }
    }
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }

  startTimer(): void {
    if (this.intervalId !== null) return;
    this.intervalId = setInterval(() => {
      this.elapsedSeconds++;
    }, 1000);
  }

  stopTimer(): void {
    if (this.intervalId !== null) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }

  resetTimer(): void {
    this.stopTimer();
    this.elapsedSeconds = 0;
  }

  get formattedTime(): string {
    const minutes = Math.floor(this.elapsedSeconds / 60);
    const seconds = this.elapsedSeconds % 60;
    return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
  }

  onNewGame(): void {
    this.resetTimer();
    this.startTimer();
    this.newGame.emit(this.selectedDifficulty);
  }

  onHint(): void {
    this.hint.emit();
  }

  onSolve(): void {
    this.stopTimer();
    this.solve.emit();
  }

  onReset(): void {
    this.resetTimer();
    this.startTimer();
    this.reset.emit();
  }
}
