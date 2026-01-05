package com.snappapp.snapng.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponseDTO {
    private Long id;
    private String publicId;
    private String url;
    private String mediaType;
    private String description;
}
