package com.sparta.team2project.notify.entity;

import com.sparta.team2project.commons.timestamped.TimeStamped;
import com.sparta.team2project.users.Users;
import jakarta.persistence.*;
import lombok.Builder;
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
    // @Embedded 상세 데이터를 하나의 객체로 묶을 때 사용
    @Embedded
    private String content;

    // 관련 링크 비어있지 않아야 한다
    @Embedded
    private RelatedURL url;

    // 읽었는지 여부 확인
    @Column(nullable = false)
    private Boolean isRead;

    // 알림 종류에 관한것
    //@Enumerated(EnumType.STRING) enum 이름을 DB에 저장
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotifyType notifyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users users;

    @Builder
    public Notify(Users users, NotifyType notifyType, String content, String url, Boolean isRead) {
        this.users = users;
        this.notifyType = notifyType;
        this.content = content;
        this.url = new RelatedURL(url);
        this.isRead = isRead;
    }
}