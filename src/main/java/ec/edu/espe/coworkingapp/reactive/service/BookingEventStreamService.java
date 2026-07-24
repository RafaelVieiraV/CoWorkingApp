package ec.edu.espe.coworkingapp.reactive.service;

import ec.edu.espe.coworkingapp.dto.response.BookingResponseDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class BookingEventStreamService {

    // Multicast sink for real-time booking updates
    private final Sinks.Many<BookingResponseDto> sink = Sinks.many().multicast().directBestEffort();

    public void publish(BookingResponseDto booking) {
        sink.tryEmitNext(booking);
    }

    public Flux<BookingResponseDto> stream() {
        return sink.asFlux();
    }
}
