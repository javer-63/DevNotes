package com.example.DevNotes.utils;

import com.example.DevNotes.dtos.ImageResponse;
import com.example.DevNotes.dtos.PostRequest;
import com.example.DevNotes.dtos.PostResponse;
import com.example.DevNotes.dtos.PostSummary;
import com.example.DevNotes.models.Image;
import com.example.DevNotes.models.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {

    PostResponse toResponse(Post post);

    PostSummary toSummary(Post post);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "images", ignore = true)
    Post toEntity(PostRequest request);

    ImageResponse toImageResponse(Image image);

    List<ImageResponse> toImageResponseList(List<Image> images);
}