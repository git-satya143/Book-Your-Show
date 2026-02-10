package com.cfss.BMSP.repository;

import com.cfss.BMSP.model.Booking;
import com.cfss.BMSP.model.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenRepository extends JpaRepository<Screen,Long> {
    List<Screen> findByTheatreId(String theatreId);

}
