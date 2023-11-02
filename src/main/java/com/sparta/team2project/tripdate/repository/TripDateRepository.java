package com.sparta.team2project.tripdate.repository;

import com.sparta.team2project.tripdate.entity.TripDate;
import com.sparta.team2project.posts.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripDateRepository extends JpaRepository<TripDate, Long> {

    List<TripDate> findByPosts(Posts posts);
}
