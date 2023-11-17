package com.sparta.team2project.comments.repository;


import com.sparta.team2project.comments.entity.Comments;
import com.sparta.team2project.posts.entity.Posts;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentsRepository extends JpaRepository<Comments, Long> {

    List<Comments> findByPosts(Posts posts);

    int countByPosts(Posts posts);

    Slice<Comments> findByPosts_IdOrderByCreatedAtDesc(Long postId, Pageable pageable);

    Slice<Comments> findAllByAndEmailOrderByCreatedAtDesc(String email, Pageable pageable);

    List<Comments> findByEmail(String email);

    Optional<Comments> findFirstByPosts_IdOrderByCreatedAtDesc(Long postId);
}

