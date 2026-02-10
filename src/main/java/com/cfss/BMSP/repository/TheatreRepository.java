package com.cfss.BMSP.repository;

import com.cfss.BMSP.model.Show;
import com.cfss.BMSP.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre,Long> {
    List<Theatre> findByCity(String city);

}
