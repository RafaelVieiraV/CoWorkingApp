package ec.edu.espe.coworkingapp.web.controller;



import ec.edu.espe.coworkingapp.domain.BookingStatus;

import ec.edu.espe.coworkingapp.dto.request.BookingRequestDto;

import ec.edu.espe.coworkingapp.dto.response.BookingResponseDto;

import ec.edu.espe.coworkingapp.service.BookingService;

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



    public BookingController(BookingService bookingService) {

        this.bookingService = bookingService;

    }



    @PostMapping

    public ResponseEntity<BookingResponseDto> create(@Valid @RequestBody BookingRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(dto));

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

}

