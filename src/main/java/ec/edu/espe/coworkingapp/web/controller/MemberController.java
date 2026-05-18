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

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.data.web.PageableDefault;



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



    @GetMapping("/search")
    public ResponseEntity<Page<MemberResponseDto>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(memberService.searchPage(name, active, pageable));
    }
    @PutMapping("/{id}")
    public ResponseEntity<MemberResponseDto> update(@PathVariable Long id, @Valid @RequestBody MemberRequestDto dto) {
        return ResponseEntity.ok(memberService.update(id, dto));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
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

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        memberService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/bookings")

    public ResponseEntity<List<BookingResponseDto>> findMemberBookings(@PathVariable Long id) {

        return ResponseEntity.ok(bookingService.findByMember(id));

    }

}
