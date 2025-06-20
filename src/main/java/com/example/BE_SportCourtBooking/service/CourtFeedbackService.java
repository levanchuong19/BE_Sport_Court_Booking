package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Court;
import com.example.BE_SportCourtBooking.entity.CourtFeedback;
import com.example.BE_SportCourtBooking.exception.ResourceNotFoundException;
import com.example.BE_SportCourtBooking.model.Request.FeedbackRequest;
import com.example.BE_SportCourtBooking.model.Response.CourtFeedbackResponse;
import com.example.BE_SportCourtBooking.repository.AccountRepository;
import com.example.BE_SportCourtBooking.repository.CourtFeedbackRepository;
import com.example.BE_SportCourtBooking.repository.CourtRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourtFeedbackService {

    CourtFeedbackRepository feedbackRepository;

    CourtRepository courtRepository;

    AccountRepository accountRepository;

    ModelMapper modelMapper = new ModelMapper();

    public CourtFeedback createFeedback(FeedbackRequest request) {
        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        CourtFeedback feedback = modelMapper.map(request, CourtFeedback.class);
        feedback.setCourt(court);
        feedback.setAccount(account);

        return feedbackRepository.save(feedback);
    }

    public List<CourtFeedbackResponse> getFeedbacksByCourt(UUID courtId) {
        return feedbackRepository.findByCourt_Id(courtId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<CourtFeedbackResponse> getFeedbacksByCurrentAccount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return feedbackRepository.findByAccount_Id(account.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private CourtFeedbackResponse convertToResponse(CourtFeedback feedback) {
        CourtFeedbackResponse dto = new CourtFeedbackResponse();
        dto.setId(feedback.getId());
        dto.setOverallRating(feedback.getOverallRating());
        dto.setComment(feedback.getComment());
        dto.setCourtQualityRating(feedback.getCourtQualityRating());
        dto.setCleanlinessRating(feedback.getCleanlinessRating());
        dto.setBookingExperienceRating(feedback.getBookingExperienceRating());
        dto.setPlayedDate(feedback.getPlayedDate());
        dto.setAnonymous(feedback.isAnonymous());

        if (!feedback.isAnonymous() && feedback.getAccount() != null) {
            dto.setFullName(feedback.getAccount().getFullName());
            dto.setAvatar(feedback.getAccount().getImage());
        }

        return dto;
    }

}
