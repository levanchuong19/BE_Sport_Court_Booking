package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.Support;
import com.example.BE_SportCourtBooking.model.Request.SupportRequest;
import com.example.BE_SportCourtBooking.model.Response.SupportResponse;
import com.example.BE_SportCourtBooking.repository.SupportRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SupportService {
    SupportRepository supportRepository;

    public SupportResponse createSupport(SupportRequest request) {
        Support support = new Support();
        support.setFullName(request.getFullName());
        support.setEmail(request.getEmail());
        support.setPhone(request.getPhoneNumber());
        support.setContent(request.getContent()); // Enum type
        support.setDescription(request.getDescription());

        Support saved = supportRepository.save(support);

        return toResponse(saved);
    }

    @PreAuthorize("hasRole('MANAGER')")
    public List<SupportResponse> getAllFeedback() {
        return supportRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('MANAGER')")
    public SupportResponse getFeedbackById(UUID id) {
        Support support = supportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Support not found"));
        return toResponse(support);
    }

    @PreAuthorize("hasRole('MANAGER')")
    public SupportResponse deleteSupport(UUID id) {
        Support support = supportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Support not found"));

        supportRepository.delete(support);

        return toResponse(support);
    }

    private SupportResponse toResponse(Support support) {
        return new SupportResponse(
                support.getFullName(),
                support.getEmail(),
                support.getPhone(),
                support.getContent(), // Enum
                support.getDescription()
        );
    }
}
