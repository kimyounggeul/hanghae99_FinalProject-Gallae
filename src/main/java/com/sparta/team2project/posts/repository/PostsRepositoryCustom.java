package com.sparta.team2project.posts.repository;

import com.sparta.team2project.posts.entity.Posts;
import com.sparta.team2project.users.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface PostsRepositoryCustom {
    Page<Posts> findAllPosts(Pageable pageable);

    Set<Posts> searchKeyword(String keyword);

    Page<Posts> findUsersLikePosts(Users existUser,Pageable pageable);

    List<Long>  findUsersLikePostsId(Users existUser);
}
