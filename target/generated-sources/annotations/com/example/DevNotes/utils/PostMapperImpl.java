package com.example.DevNotes.utils;

import com.example.DevNotes.dtos.ImageResponse;
import com.example.DevNotes.dtos.PostRequest;
import com.example.DevNotes.dtos.PostResponse;
import com.example.DevNotes.dtos.PostSummary;
import com.example.DevNotes.models.Image;
import com.example.DevNotes.models.Post;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-02T15:46:16+0400",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12 (Ubuntu)"
)
@Component
public class PostMapperImpl implements PostMapper {

    @Override
    public PostResponse toResponse(Post post) {
        if ( post == null ) {
            return null;
        }

        Long id = null;
        String url = null;
        String title = null;
        String description = null;
        String content = null;
        byte time = 0;
        LocalDateTime createdAt = null;
        long views = 0L;
        List<ImageResponse> images = null;

        id = post.getId();
        url = post.getUrl();
        title = post.getTitle();
        description = post.getDescription();
        content = post.getContent();
        time = post.getTime();
        createdAt = post.getCreatedAt();
        views = post.getViews();
        images = toImageResponseList( post.getImages() );

        PostResponse postResponse = new PostResponse( id, url, title, description, content, time, createdAt, views, images );

        return postResponse;
    }

    @Override
    public PostSummary toSummary(Post post) {
        if ( post == null ) {
            return null;
        }

        Long id = null;
        String url = null;
        String title = null;
        String description = null;
        byte time = 0;
        LocalDateTime createdAt = null;
        long views = 0L;

        id = post.getId();
        url = post.getUrl();
        title = post.getTitle();
        description = post.getDescription();
        time = post.getTime();
        createdAt = post.getCreatedAt();
        views = post.getViews();

        PostSummary postSummary = new PostSummary( id, url, title, description, time, createdAt, views );

        return postSummary;
    }

    @Override
    public Post toEntity(PostRequest request) {
        if ( request == null ) {
            return null;
        }

        Post post = new Post();

        post.setUrl( request.url() );
        post.setTitle( request.title() );
        post.setDescription( request.description() );
        post.setContent( request.content() );
        post.setTime( request.time() );

        return post;
    }

    @Override
    public ImageResponse toImageResponse(Image image) {
        if ( image == null ) {
            return null;
        }

        Long id = null;
        String fileName = null;
        String url = null;

        id = image.getId();
        fileName = image.getFileName();
        url = image.getUrl();

        ImageResponse imageResponse = new ImageResponse( id, fileName, url );

        return imageResponse;
    }

    @Override
    public List<ImageResponse> toImageResponseList(List<Image> images) {
        if ( images == null ) {
            return null;
        }

        List<ImageResponse> list = new ArrayList<ImageResponse>( images.size() );
        for ( Image image : images ) {
            list.add( toImageResponse( image ) );
        }

        return list;
    }
}
