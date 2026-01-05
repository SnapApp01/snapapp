//package com.snappapp.snapng.controller;
//
//import com.snappapp.snapng.dto.GenericResponse;
//import com.snappapp.snapng.dto.request.MediaUpdateRequest;
//import com.snappapp.snapng.dto.request.MediaUploadRequest;
//import com.snappapp.snapng.services.MediaService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/media")
//public class MediaController {
//    private final MediaService mediaService;
//
//    public MediaController(MediaService mediaService) {
//        this.mediaService = mediaService;
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<GenericResponse> getMedia(@PathVariable Long id) {
//        GenericResponse response = mediaService.getMedia(id);
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @GetMapping
//    public ResponseEntity<GenericResponse> getAllMedia() {
//        GenericResponse response = mediaService.getAllMedia();
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @PostMapping("/upload")
//    public ResponseEntity<GenericResponse> uploadMedia(@ModelAttribute MediaUploadRequest request) {
//        GenericResponse response = mediaService.uploadMedia(request);
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<GenericResponse> updateMedia(@PathVariable Long id, @ModelAttribute MediaUpdateRequest request) {
//        GenericResponse response = mediaService.updateMedia(id, request);
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<GenericResponse> deleteMedia(@PathVariable Long id) {
//        GenericResponse response = mediaService.deleteMedia(id);
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//}