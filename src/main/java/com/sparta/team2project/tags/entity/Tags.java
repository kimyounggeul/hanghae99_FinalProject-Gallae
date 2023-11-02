package com.sparta.team2project.tags.entity;

import com.sparta.team2project.posts.entity.Posts;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor
public class Tags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String purpose;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="posts_id")
    private Posts posts;

    public Tags(String purpose, Posts posts) {
        this.purpose = purpose;
        this.posts = posts;
    }

    //    public void updateTags(UpdateRequestDto updateRequestDto) {
//        this.purpose = updateRequestDto.getPurpose();
//    }
}