package ec.edu.espe.coworkingapp.service.impl;

import ec.edu.espe.coworkingapp.domain.Member;
import ec.edu.espe.coworkingapp.dto.request.MemberRequest;
import ec.edu.espe.coworkingapp.dto.response.MemberResponse;
import ec.edu.espe.coworkingapp.repository.MemberRepository;
import ec.edu.espe.coworkingapp.service.MemberService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public List<MemberResponse> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public MemberResponse findById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Miembro no encontrado con id: " + id));
        return MemberResponse.from(member);
    }

    @Override
    public MemberResponse create(MemberRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe un miembro con el email: " + request.getEmail());
        }
        Member member = new Member();
        member.setFullName(request.getFullName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        member.setPlanType(request.getPlanType());
        member.setMonthlyHoursQuota(request.getMonthlyHoursQuota());
        return MemberResponse.from(memberRepository.save(member));
    }

    @Override
    public MemberResponse update(Long id, MemberRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Miembro no encontrado con id: " + id));
        member.setFullName(request.getFullName());
        member.setPhone(request.getPhone());
        member.setPlanType(request.getPlanType());
        member.setMonthlyHoursQuota(request.getMonthlyHoursQuota());
        return MemberResponse.from(memberRepository.save(member));
    }

    @Override
    public void delete(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new RuntimeException("Miembro no encontrado con id: " + id);
        }
        memberRepository.deleteById(id);
    }
}