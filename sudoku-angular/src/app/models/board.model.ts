export interface BoardDto {
  values: number[][];
  given: boolean[][];
  conflicts: boolean[][];
  complete: boolean;
  mistakeCount: number;
  difficulty: string;
}

export interface HintResponse {
  row?: number;
  col?: number;
  value?: number;
  reason?: string;
  board?: BoardDto;
  message?: string;
}
