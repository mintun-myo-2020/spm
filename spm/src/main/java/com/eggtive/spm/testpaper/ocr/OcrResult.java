package com.eggtive.spm.testpaper.ocr;

import java.util.List;

public record OcrResult(List<OcrTextBlock> textBlocks, String rawText, String status, float confidence) {}
