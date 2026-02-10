package com.cfss.BMSP.repository;

import com.cfss.BMSP.model.Show;
import com.cfss.BMSP.model.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat,Long> {
    List<ShowSeat> findByShowId(Long ShowId);

    List<ShowSeat> findByShowAndStatus(Long Show,String status);

}
