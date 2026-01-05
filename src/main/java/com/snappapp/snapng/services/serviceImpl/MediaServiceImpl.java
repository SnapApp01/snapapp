package com.snappapp.snapng.services.serviceImpl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.snappapp.snapng.dto.GenericResponse;
import com.snappapp.snapng.dto.request.MediaUpdateRequest;
import com.snappapp.snapng.dto.request.MediaUploadRequest;
import com.snappapp.snapng.exceptions.InvalidMediaTypeException;
import com.snappapp.snapng.exceptions.MediaNotFoundException;
import com.snappapp.snapng.exceptions.MediaUploadFailedException;
import com.snappapp.snapng.models.Media;
import com.snappapp.snapng.repository.MediaRepository;
import com.snappapp.snapng.services.MediaService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MediaServiceImpl implements MediaService {

    private final Cloudinary cloudinary;
    private final MediaRepository mediaRepository;

    public MediaServiceImpl(Cloudinary cloudinary, MediaRepository mediaRepository) {
        this.cloudinary = cloudinary;
        this.mediaRepository = mediaRepository;
    }

    @Override
    public GenericResponse getMedia(Long id) {
        Optional<Media> media = mediaRepository.findById(id);
        if (media.isEmpty()) {
            throw new MediaNotFoundException("Media not found");
        }
        return GenericResponse.builder()
                .message("Media retrieved successfully")
                .data(media.get())
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    public GenericResponse getAllMedia() {
        List<Media> mediaList = mediaRepository.findAll();
        return GenericResponse.builder()
                .message("Media list retrieved successfully")
                .data(mediaList)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    @Transactional
    public GenericResponse uploadMedia(MediaUploadRequest request) {
        try {
            MultipartFile file = request.getFile();
            String mediaType = request.getMediaType();
            if (!mediaType.equals("image") && !mediaType.equals("video")) {
                throw new InvalidMediaTypeException("Invalid media type. Must be 'image' or 'video'.");
            }

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", mediaType
            ));

            String publicId = (String) uploadResult.get("public_id");
            String url = (String) uploadResult.get("url");

            Media media = Media.builder()
                    .publicId(publicId)
                    .url(url)
                    .mediaType(mediaType)
                    .description(request.getDescription())
                    .build();
            media.setCreatedAt(LocalDateTime.now());
            media.setUpdatedAt(LocalDateTime.now());
            mediaRepository.save(media);

            return GenericResponse.builder()
                    .message("Media uploaded successfully")
                    .data(media)
                    .httpStatus(HttpStatus.OK)
                    .build();
        } catch (IOException e) {
            throw new MediaUploadFailedException("Failed to upload media: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Media uploadImage(MediaUploadRequest request) {
        try {
            MultipartFile file = request.getFile();
            String mediaType = request.getMediaType();
            if (!mediaType.equals("image")) {
                throw new InvalidMediaTypeException("Invalid media type. Must be 'image'.");
            }
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", mediaType
            ));

            String publicId = (String) uploadResult.get("public_id");
            String url = (String) uploadResult.get("url");

            Media media = Media.builder()
                    .publicId(publicId)
                    .url(url)
                    .mediaType(mediaType)
                    .description(request.getDescription())
                    .build();
            media.setCreatedAt(LocalDateTime.now());
            media.setUpdatedAt(LocalDateTime.now());
            mediaRepository.save(media);

            return media;
        } catch (IOException e) {
            throw new MediaUploadFailedException("Failed to upload media: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public GenericResponse updateMedia(Long id, MediaUpdateRequest request) {
        Optional<Media> existingMedia = mediaRepository.findById(id);
        if (existingMedia.isEmpty()) {
            throw new MediaNotFoundException("Media not found");
        }
        Media media = existingMedia.get();
        try {
            if (request.getFile() != null && !request.getFile().isEmpty()) {
                String mediaType = request.getMediaType() != null ? request.getMediaType() : media.getMediaType();
                if (!mediaType.equals("image") && !mediaType.equals("video")) {
                    throw new InvalidMediaTypeException("Invalid media type. Must be 'image' or 'video'.");
                }
                cloudinary.uploader().destroy(media.getPublicId(), ObjectUtils.asMap(
                        "resource_type", media.getMediaType()
                ));
                Map uploadResult = cloudinary.uploader().upload(request.getFile().getBytes(), ObjectUtils.asMap(
                        "resource_type", mediaType
                ));
                media.setPublicId((String) uploadResult.get("public_id"));
                media.setUrl((String) uploadResult.get("url"));
                media.setMediaType(mediaType);
            } else if (request.getMediaType() != null) {
                if (!request.getMediaType().equals("image") && !request.getMediaType().equals("video")) {
                    throw new InvalidMediaTypeException("Invalid media type. Must be 'image' or 'video'.");
                }
                media.setMediaType(request.getMediaType());
            } else {
                throw new MediaNotFoundException("No updates provided");
            }
            mediaRepository.save(media);

            return GenericResponse.builder()
                    .message("Media updated successfully")
                    .data(media)
                    .httpStatus(HttpStatus.OK)
                    .build();
        } catch (IOException e) {
            throw new MediaUploadFailedException("Failed to upload media: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public GenericResponse deleteMedia(Long id) {
        Optional<Media> media = mediaRepository.findById(id);
        if (media.isPresent()) {
            try {
                cloudinary.uploader().destroy(media.get().getPublicId(), ObjectUtils.asMap(
                        "resource_type", media.get().getMediaType()
                ));
                mediaRepository.deleteById(id);
                return GenericResponse.builder()
                        .message("Media deleted successfully")
                        .httpStatus(HttpStatus.OK)
                        .build();
            } catch (Exception e) {
                return GenericResponse.builder()
                        .message("Failed to delete media: " + e.getMessage())
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build();
            }
        }
        return GenericResponse.builder()
                .message("Media not found")
                .httpStatus(HttpStatus.NOT_FOUND)
                .build();
    }
}