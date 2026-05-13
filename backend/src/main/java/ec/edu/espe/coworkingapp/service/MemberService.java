package ec.edu.espe.coworkingapp.service;

import ec.edu.espe.coworkingapp.dto.request.MemberRequest;
import ec.edu.espe.coworkingapp.dto.response.MemberResponse;
import java.util.List;

public interface MemberService {
    List<MemberResponse> findAll();
    MemberResponse findById(Long id);
    MemberResponse create(MemberRequest request);
    MemberResponse update(Long id, MemberRequest request);
    void delete(Long id);
}