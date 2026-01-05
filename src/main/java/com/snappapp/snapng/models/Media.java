package com.snappapp.snapng.models;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "media")
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Media extends BaseEntity {

    private String publicId;
    private String url;
    private String mediaType;
    private String description;
}