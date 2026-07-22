package ec.edu.espe.coworkingapp.service.impl;

import ec.edu.espe.coworkingapp.repository.MemberRepository;
import ec.edu.espe.coworkingapp.service.MemberBlockClient;
import org.springframework.stereotype.Service;

@Service
public class MemberBlockClientImpl implements MemberBlockClient {

    private final MemberRepository memberRepository;

    public MemberBlockClientImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean isBlocked(String email) {
        if (email == null) {
            return false;
        }
        return memberRepository.findByEmail(email)
                .map(m -> Boolean.TRUE.equals(m.getBlocked()))
                .orElse(false);
    }
}