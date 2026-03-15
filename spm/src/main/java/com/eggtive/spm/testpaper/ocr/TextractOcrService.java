package com.eggtive.spm.testpaper.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.ocr.type", havingValue = "textract")
public class TextractOcrService implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(TextractOcrService.class);

    private final TextractClient textractClient;
    private final String bucket;

    public TextractOcrService(TextractClient textractClient,
                              @Value("${app.storage.s3-bucket}") String bucket) {
        this.textractClient = textractClient;
        this.bucket = bucket;
    }

    @Override
    public OcrResult extractText(String bucketParam, String key) {
        String effectiveBucket = (bucketParam != null && !bucketParam.isBlank()) ? bucketParam : this.bucket;
        try {
            DetectDocumentTextResponse response = textractClient.detectDocumentText(
                    DetectDocumentTextRequest.builder()
                            .document(Document.builder()
                                    .s3Object(S3Object.builder().bucket(effectiveBucket).name(key).build())
                                    .build())
                            .build());

            List<OcrTextBlock> blocks = response.blocks().stream()
                    .filter(b -> b.blockType() == BlockType.LINE)
                    .map(b -> new OcrTextBlock(b.text(), b.confidence() / 100f, b.blockType().toString()))
                    .toList();

            String rawText = blocks.stream().map(OcrTextBlock::text).collect(Collectors.joining("\n"));
            float avgConfidence = blocks.isEmpty() ? 0f :
                    (float) blocks.stream().mapToDouble(OcrTextBlock::confidence).average().orElse(0);

            log.info("Textract extracted {} lines from s3://{}/{}", blocks.size(), effectiveBucket, key);
            return new OcrResult(blocks, rawText, "COMPLETED", avgConfidence);

        } catch (Exception e) {
            log.error("Textract failed for s3://{}/{}", effectiveBucket, key, e);
            return new OcrResult(List.of(), "", "FAILED", 0f);
        }
    }
}
