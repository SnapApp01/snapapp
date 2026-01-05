package com.snappapp.snapng.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaUploadRequest {
    private MultipartFile file;
    private String mediaType;
    private String description;
}