package com.sparta.team2project.notify.entity;

import com.sparta.team2project.commons.timestamped.TimeStamped;
import com.sparta.team2project.posts.entity.Posts;
import com.sparta.team2project.users.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor
public class Notify extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notify_id")
    private Long id;

    // 알림 내용 비어있지 않아야 하고 50자 이내
    private String sender;

    private String contents;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posts_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Posts posts;

    public Notify (String sender, String contents, Posts posts) {
        this.sender = sender;
        this.contents = contents;
        this.posts = posts;
    }

    public Notify (Users users) {
        this.email = users.getEmail();
    }
}