package com.sparta.team2project.notify.repository;

import com.sparta.team2project.notify.entity.Notify;
import com.sparta.team2project.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NorifyRepository extends JpaRepository<Notify, Long> {

    List<Notify> findAllByAndEmailOrderByCreatedAtDesc(Users users);
}
