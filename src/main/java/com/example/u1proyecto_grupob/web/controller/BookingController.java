package com.example.u1proyecto_grupob.web.controller;

import com.example.u1proyecto_grupob.domain.BookingStatus;
import com.example.u1proyecto_grupob.dto.BookingRequestDto;
import com.example.u1proyecto_grupob.dto.BookingResponseDto;
import com.example.u1proyecto_grupob.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<BookingResponseDto> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirm(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponseDto> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancel(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingResponseDto>> findByStatus(@PathVariable String status) {
        try {
            BookingStatus bs = BookingStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(bookingService.findByStatus(bs));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
