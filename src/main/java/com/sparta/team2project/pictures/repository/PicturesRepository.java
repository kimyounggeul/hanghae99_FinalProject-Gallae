package com.sparta.team2project.pictures.repository;

import com.sparta.team2project.pictures.entity.Pictures;
import com.sparta.team2project.schedules.entity.Schedules;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PicturesRepository extends JpaRepository<Pictures, Long> {
    void deleteByPicturesName(String picturesName);

    List<Pictures> findAllBySchedules(Schedules schedules);
}
