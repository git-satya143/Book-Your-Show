package com.cfss.BMSP.repository;

import com.cfss.BMSP.model.Booking;
import com.cfss.BMSP.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowRepository extends JpaRepository<Show,Long> {
    List<Show> findByMovieId(Long movieId);

    List<Show> findByScreenId(Long ScreenId);

    List<Show> findByShowTimeBetweeen(LocalDateTime startTime, LocalDateTime endTime);
}
