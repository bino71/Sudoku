package io.bino.web.dto;

public record HintResponse(int row, int col, int value, String reason, BoardDto board) {}
