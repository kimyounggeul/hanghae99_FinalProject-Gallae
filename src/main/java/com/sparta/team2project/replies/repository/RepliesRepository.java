package com.sparta.team2project.replies.repository;

import com.sparta.team2project.replies.entity.Replies;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepliesRepository extends JpaRepository<Replies, Long> {

    Slice<Replies> findByComments_IdOrderByCreatedAtDesc(Long commentId, Pageable pageable);

    Slice<Replies> findAllByAndEmailOrderByCreatedAtDesc(String email, Pageable pageable);

    List<Replies> findByEmail(String email);
}
