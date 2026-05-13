package ec.edu.espe.coworkingapp.service;

import ec.edu.espe.coworkingapp.dto.request.BookingRequest;
import ec.edu.espe.coworkingapp.dto.response.BookingResponse;
import java.util.List;

public interface BookingService {
    List<BookingResponse> findAll();
    BookingResponse findById(Long id);
    BookingResponse create(BookingRequest request);
    BookingResponse cancel(Long id);
    List<BookingResponse> findByMember(Long memberId);
}