import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BoardDto, HintResponse } from '../models/board.model';

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/game';

  newGame(difficulty: string): Observable<BoardDto> {
    return this.http.get<BoardDto>(`${this.baseUrl}/new`, {
      params: { difficulty }
    });
  }

  getState(): Observable<BoardDto | null> {
    return this.http.get<BoardDto | null>(`${this.baseUrl}/state`);
  }

  enterValue(row: number, col: number, value: number): Observable<BoardDto> {
    return this.http.post<BoardDto>(`${this.baseUrl}/enter`, { row, col, value });
  }

  hint(): Observable<HintResponse> {
    return this.http.post<HintResponse>(`${this.baseUrl}/hint`, {});
  }

  solve(): Observable<BoardDto> {
    return this.http.post<BoardDto>(`${this.baseUrl}/solve`, {});
  }

  reset(): Observable<BoardDto> {
    return this.http.post<BoardDto>(`${this.baseUrl}/reset`, {});
  }
}
