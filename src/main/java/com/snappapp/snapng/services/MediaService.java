package com.snappapp.snapng.services;

import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.dto.request.MediaUpdateRequest;
import com.snappapp.snapng.dto.request.MediaUploadRequest;
import com.snappapp.snapng.models.Media;
import org.springframework.stereotype.Component;

@Component
public interface MediaService {
    GenericResponse getMedia(Long id);
    GenericResponse getAllMedia();
    GenericResponse updateMedia(Long id, MediaUpdateRequest request);
    GenericResponse uploadMedia(MediaUploadRequest request);
    Media uploadImage(MediaUploadRequest request);
    GenericResponse deleteMedia(Long id);
}