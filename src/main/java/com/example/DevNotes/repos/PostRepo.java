package com.example.DevNotes.repos;


import com.example.DevNotes.models.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepo extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = "images")
    Optional<Post> findByUrl(String url);

    Post findTopByOrderByCreatedAtDesc();

    boolean existsByUrl(String url);

    @Modifying
    @Query("update Post p set p.views = p.views + 1 where p.url = :url")
    void incrementViews(@Param("url") String url);

    List<Post> findTop5ByOrderByViewsDesc();
}