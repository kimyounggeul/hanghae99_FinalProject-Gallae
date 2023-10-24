package com.sparta.team2project.comments.entity;

import com.sparta.team2project.comments.dto.CommentsRequestDto;
import com.sparta.team2project.commons.timestamped.TimeStamped;
import com.sparta.team2project.posts.entity.Posts;
import com.sparta.team2project.replies.entity.Replies;
import com.sparta.team2project.users.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "comments")
public class Comments extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="comments_id")
    private  Long id;

    private String email;

    @Column(nullable = false, length = 500)
    private String contents;

    @ManyToOne
    @JoinColumn(name = "posts_id")
    private Posts posts;

    @OneToMany (mappedBy = "comments", orphanRemoval = true)
    private List<Replies> repliesList= new ArrayList<>();


    public Comments(CommentsRequestDto requestDto, Users users, Posts posts) {
        this.contents = requestDto.getContents();
        this.posts = posts;
        this.email = users.getEmail();
    }

    public void update(CommentsRequestDto requestDto, Users users) {
        this.contents = requestDto.getContents();
    }
}
