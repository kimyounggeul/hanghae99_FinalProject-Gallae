package com.sparta.team2project.posts.repository;

import com.sparta.team2project.posts.entity.Posts;
import com.sparta.team2project.posts.entity.PostsPictures;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostsPicturesRepository extends JpaRepository <PostsPictures, Long> {

    List<PostsPictures> findByPosts(Posts posts);
}
