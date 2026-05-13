package ec.edu.espe.coworkingapp.web.controller;

import ec.edu.espe.coworkingapp.dto.request.MemberRequestDto;
import ec.edu.espe.coworkingapp.dto.response.MemberResponseDto;
import ec.edu.espe.coworkingapp.dto.response.BookingResponseDto;
import ec.edu.espe.coworkingapp.service.MemberService;
import ec.edu.espe.coworkingapp.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final BookingService bookingService;

    public MemberController(MemberService memberService, BookingService bookingService) {
        this.memberService = memberService;
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<MemberResponseDto> create(@Valid @RequestBody MemberRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<MemberResponseDto>> findAll() {
        return ResponseEntity.ok(memberService.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<MemberResponseDto>> findAllActive() {
        return ResponseEntity.ok(memberService.findAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.findById(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        memberService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/bookings")
    public ResponseEntity<List<BookingResponseDto>> findMemberBookings(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.findByMember(id));
    }
}