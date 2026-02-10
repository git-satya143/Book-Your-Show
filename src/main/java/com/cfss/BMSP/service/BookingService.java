package com.cfss.BMSP.service;

import com.cfss.BMSP.dto.*;
import com.cfss.BMSP.exception.ResourceNotFoundException;
import com.cfss.BMSP.exception.SeatUnavailableException;
import com.cfss.BMSP.model.*;
import com.cfss.BMSP.repository.BookingRepository;
import com.cfss.BMSP.repository.ShowRepository;
import com.cfss.BMSP.repository.ShowSeatRepository;
import com.cfss.BMSP.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Transactional
    public BookingDto createBooking(BookingRequestDto bookingRequest)
    {
        User user= userRepository.findById(bookingRequest.getUserId())
                .orElseThrow(()-> new ResourceNotFoundException("User Not Found"));


        Show show= showRepository
                .findById(bookingRequest.getShowId())
                .orElseThrow(()-> new ResourceNotFoundException("Show Not Found"));

        List<ShowSeat> selectedSeats= showSeatRepository.findAllById(bookingRequest.getSeatIds());

        for(ShowSeat seat:selectedSeats)
        {
            if (!"AVAILABLE".equals(seat.getStatus()))
            {
                throw  new SeatUnavailableException("Seat"+ seat.getSeat().getSeatNumber()+"is not available");
            }
            seat.setStatus("locked");
        }
        showSeatRepository.saveAll(selectedSeats);
        Double totalAmount = selectedSeats.stream()
                .mapToDouble(ShowSeat::getPrice)
                .sum();
        //payment
        Payment payment= new Payment();
        payment.setAmount(totalAmount);
        payment.setPaymentMethod(bookingRequest.getPaymentMethod());
        payment.setPaymentTime(LocalDateTime.now());
        payment.setStatus("SUCCESS");
        payment.setTansactionId(UUID.randomUUID().toString());

        //Booking
        Booking booking= new Booking();
        booking.setUser(user);
        booking.setShow(show);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("CONFIRMED");
        booking.setTotalAmount(totalAmount);
        booking.setBookingNumber(UUID.randomUUID().toString());
        booking.setPayment(payment);

        Booking saveBooking= bookingRepository.save(booking);

        selectedSeats.forEach(seat->
        {
            seat.setStatus("BOOKED");
            seat.setBooking(saveBooking);
        });
        return mapToBookingDto(saveBooking,selectedSeats);
    }

    public BookingDto getBookingById(Long id){

        Booking booking= bookingRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Booking Not Found"));
        List<ShowSeat> seats = showSeatRepository.findAll()
                .stream()
                .filter(seat->seat.getBooking()!=null && seat.getBooking().getId().equals(booking.getId()))
                .collect(Collectors.toList());
        return mapToBookingDto(booking,seats);
    }

    public BookingDto getBookingByNumber(String bookingNumber)
    {
        Booking booking= bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(()-> new ResourceNotFoundException("Booking Not Found"));
        List<ShowSeat> seats = showSeatRepository.findAll()
                .stream()
                .filter(seat->seat.getBooking()!=null && seat.getBooking().getId().equals(booking.getId()))
                .collect(Collectors.toList());
        return mapToBookingDto(booking,seats);
    }

    private List<BookingDto> getBookingByUserId(Long  userId)
    {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream()
                .map(booking ->
                {
                    List<ShowSeat> seats=showSeatRepository.findAll()
                            .stream()
                            .filter(seat-> seat.getBooking()!= null && seat.getBooking().getId().equals(booking.getId()))
                            .collect(Collectors.toList());
                    return mapToBookingDto(booking,seats);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingDto cancelBooking(Long id)
    {
        Booking booking= bookingRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("BOOKING NOT FOUND"));
        booking.setStatus("CANCELLED");

        List<ShowSeat> seats= showSeatRepository.findAll()
                .stream()
                .filter(seat->seat.getBooking()!=null && seat.getBooking().getId().equals(booking.getId()))
                .collect(Collectors.toList());
        seats.forEach(seat->
        {
            seat.setStatus("AVAILABLE");
            seat.setBooking(null);
        });

        if(booking.getPayment()!= null)
        {
            booking.getPayment().setStatus("REFUNDED");
        }

        Booking updateBooking = bookingRepository.save(booking);
        showSeatRepository.saveAll(seats);
        return mapToBookingDto(updateBooking,seats);

    }
    private BookingDto mapToBookingDto(Booking booking, List<ShowSeat> seats)
    {
        BookingDto bookingDto= new BookingDto();

        bookingDto.setId(booking.getId());
        bookingDto.setBookingNumber(booking.getBookingNumber());
        bookingDto.setBookingTime(booking.getBookingTime());
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setTotalAmount(booking.getTotalAmount());

        //User
        UserDto userDto=new UserDto();
        userDto.setId(booking.getUser().getId());
        userDto.setName(booking.getUser().getName());
        userDto.setEmail(booking.getUser().getEmail());
        userDto.setPhoneNumber(booking.getUser().getPhoneNumber());
        bookingDto.setUser(userDto);

        //Show
        ShowDto showDto= new ShowDto();

        showDto.setId(booking.getShow().getId());
        showDto.setStartTime(booking.getShow().getStartTime());
        showDto.setEndTime(booking.getShow().getEndTime());

        //Movie
        MovieDto movieDto= new MovieDto();

        movieDto.setId(booking.getShow().getMovie().getId());
        movieDto.setTitle(booking.getShow().getMovie().getTitle());
        movieDto.setGenre(booking.getShow().getMovie().getGenre());
        movieDto.setLanguage(booking.getShow().getMovie().getLanguage());
        movieDto.setDescription(booking.getShow().getMovie().getDescription());
        movieDto.setDurationMins(booking.getShow().getMovie().getDurationMins());
        movieDto.setPosterUrl(booking.getShow().getMovie().getPosterUrl());
        movieDto.setReleaseDate(booking.getShow().getMovie().getReleaseDate());
        showDto.setMovie(movieDto);

        //screen
        ScreenDto screenDto= new ScreenDto();

        screenDto.setId(bookingDto.getShow().getScreen().getId());
        screenDto.setName(bookingDto.getShow().getScreen().getName());
        screenDto.setTotalSeats(bookingDto.getShow().getScreen().getTotalSeats());

        //Theatre
        TheatreDto theatreDto=new TheatreDto();

        theatreDto.setId(booking.getShow().getScreen().getTheatre().getId());
        theatreDto.setName(booking.getShow().getScreen().getTheatre().getName());
        theatreDto.setAddress(booking.getShow().getScreen().getTheatre().getAddress());
        theatreDto.setTotalScreens(booking.getShow().getScreen().getTheatre().getTotalScreen());
        theatreDto.setCity(booking.getShow().getScreen().getTheatre().getCity());

        screenDto.setTheatre(theatreDto);
        showDto.setScreen(screenDto);
        bookingDto.setShow(showDto);

        List<ShowSeatDto> seatDtos = seats.stream()
                .map(seat->
                {
                    ShowSeatDto seatDto= new ShowSeatDto();
                    seatDto.setId(seat.getId());
                    seatDto.setStatus(seat.getStatus());
                    seatDto.setPrice(seat.getPrice());

                    SeatDto baseSeatDto = new SeatDto();
                    baseSeatDto.setId(seat.getSeat().getId());
                    baseSeatDto.setSeatNumber(seat.getSeat().getSeatNumber());
                    baseSeatDto.setSeatType(seat.getSeat().getSeatType());
                    baseSeatDto.setBasePrice(seat.getSeat().getBasePrice());
                    seatDto.setSeat(baseSeatDto);

                    return seatDto;
                })
                .collect(Collectors.toList());
                bookingDto.setSeats(seatDtos);

        if (booking.getPayment()!= null)
        {
            PaymentDto paymentDto =new PaymentDto();
            paymentDto.setId(booking.getPayment().getId());
            paymentDto.setAmount(booking.getPayment().getAmount());
            paymentDto.setPaymentTime(booking.getPayment().getPaymentTime());
            paymentDto.setPaymentMethod(booking.getPayment().getPaymentMethod());
            paymentDto.setStatus(booking.getPayment().getStatus());
            paymentDto.setTransactionId(booking.getPayment().getTansactionId());
            bookingDto.setPayment(paymentDto);


        }
        return bookingDto;


    }
}




















