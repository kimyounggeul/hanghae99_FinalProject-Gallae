package com.sparta.team2project.posts.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PostsPictures")
@Getter
@NoArgsConstructor
public class PostsPictures {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String postsPicturesURL;

    @Column(nullable = true)
    private String postsPicturesName;

    @Column(nullable = true)
    private String postsPictureContentType;

    @Column(nullable = true)
    private Long postsPictureSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posts_id",nullable = false)
    private Posts posts;

    public PostsPictures(
            Posts posts,
            String postsPicturesURL,
            String postsPicturesName,
            String postsPictureContentType,
            Long postsPictureSize
    ){
        this.posts = posts;
        this.postsPicturesURL = postsPicturesURL;
        this.postsPicturesName = postsPicturesName;
        this.postsPictureContentType = postsPictureContentType;
        this.postsPictureSize = postsPictureSize;
    }

    public void updatePostsPictures(
            String postsPicturesURL,
            String postsPicturesName,
            String postsPictureContentType,
            Long postsPictureSize
    ){
        this.postsPicturesURL = postsPicturesURL;
        this.postsPicturesName = postsPicturesName;
        this.postsPictureContentType = postsPictureContentType;
        this.postsPictureSize = postsPictureSize;
    }
}
