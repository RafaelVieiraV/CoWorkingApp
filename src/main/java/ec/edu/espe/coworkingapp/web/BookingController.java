package ec.edu.espe.coworkingapp.web;



import ec.edu.espe.coworkingapp.domain.BookingStatus;

import ec.edu.espe.coworkingapp.dto.request.BookingRequestDto;

import ec.edu.espe.coworkingapp.dto.response.BookingResponseDto;

import ec.edu.espe.coworkingapp.pubsub.BookingPipelineResult;
import ec.edu.espe.coworkingapp.pubsub.BookingPipelineService;
import ec.edu.espe.coworkingapp.service.BookingService;
import ec.edu.espe.coworkingapp.reactive.service.BookingEventStreamService;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;



import java.util.List;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.data.web.PageableDefault;



@RestController

@RequestMapping("/api/bookings")

public class BookingController {



    private final BookingService bookingService;
    private final BookingPipelineService bookingPipelineService;
    private final BookingEventStreamService bookingEventStreamService;


    public BookingController(BookingService bookingService, BookingPipelineService bookingPipelineService, BookingEventStreamService bookingEventStreamService) {
        this.bookingService = bookingService;
        this.bookingPipelineService = bookingPipelineService;
        this.bookingEventStreamService = bookingEventStreamService;
    }



    @PostMapping

    public ResponseEntity<BookingCreationResponse> create(@Valid @RequestBody BookingRequestDto dto) {
        BookingResponseDto bookingCreado = bookingService.create(dto);
        BookingPipelineResult verificacion = bookingPipelineService.verificarReservasReales();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BookingCreationResponse(bookingCreado, verificacion));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BookingResponseDto>> search(
            @RequestParam(required = false) Long id,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(bookingService.searchPage(id, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponseDto> update(@PathVariable Long id, @Valid @RequestBody BookingRequestDto dto) {
        return ResponseEntity.ok(bookingService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> findAll() {

        return ResponseEntity.ok(bookingService.findAll());

    }

    @GetMapping("/{id}")

    public ResponseEntity<BookingResponseDto> findById(@PathVariable Long id) {

        return ResponseEntity.ok(bookingService.findById(id));

    }

    @GetMapping("/member/{memberId}")

    public ResponseEntity<List<BookingResponseDto>> findByMember(@PathVariable Long memberId) {

        return ResponseEntity.ok(bookingService.findByMember(memberId));

    }

    @GetMapping("/workspace/{workspaceId}")

    public ResponseEntity<List<BookingResponseDto>> findByWorkspace(@PathVariable Long workspaceId) {

        return ResponseEntity.ok(bookingService.findByWorkspace(workspaceId));

    }

    @GetMapping("/status/{status}")

    public ResponseEntity<List<BookingResponseDto>> findByStatus(@PathVariable String status) {

        try {

            BookingStatus enumStatus = BookingStatus.valueOf(status.toUpperCase());

            return ResponseEntity.ok(bookingService.findByStatus(enumStatus));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().build();

        }

    }

    @PatchMapping("/{id}/confirm")

    public ResponseEntity<BookingResponseDto> confirm(@PathVariable Long id) {

        return ResponseEntity.ok(bookingService.confirm(id));

    }

    @PatchMapping("/{id}/cancel")

    public ResponseEntity<BookingResponseDto> cancel(@PathVariable Long id) {

        return ResponseEntity.ok(bookingService.cancel(id));

    }
     
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BookingResponseDto> stream() {
        return bookingEventStreamService.stream();
    }
     
    //Respuesta: la reserva recién creada + el resultado de la verificación reactiva sobre todas las reservas
    public record BookingCreationResponse(
            BookingResponseDto booking,
            BookingPipelineResult verificacionReactiva
    ) {
    }

}