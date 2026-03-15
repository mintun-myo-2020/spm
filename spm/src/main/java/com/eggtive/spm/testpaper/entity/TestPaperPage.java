package com.eggtive.spm.testpaper.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import com.eggtive.spm.testpaper.enums.PageStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "test_paper_pages", uniqueConstraints = @UniqueConstraint(columnNames = {"upload_id", "page_number"}))
public class TestPaperPage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_id", nullable = false)
    private TestPaperUpload upload;

    @Column(nullable = false)
    private int pageNumber;

    @Column(name = "storage_location", nullable = false, length = 255)
    private String storageLocation;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private long fileSizeBytes;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Column(columnDefinition = "TEXT")
    private String parsedResult;

    private Float ocrConfidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PageStatus status = PageStatus.PENDING;

    public TestPaperUpload getUpload() { return upload; }
    public void setUpload(TestPaperUpload upload) { this.upload = upload; }
    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    public String getParsedResult() { return parsedResult; }
    public void setParsedResult(String parsedResult) { this.parsedResult = parsedResult; }
    public Float getOcrConfidence() { return ocrConfidence; }
    public void setOcrConfidence(Float ocrConfidence) { this.ocrConfidence = ocrConfidence; }
    public PageStatus getStatus() { return status; }
    public void setStatus(PageStatus status) { this.status = status; }
}
