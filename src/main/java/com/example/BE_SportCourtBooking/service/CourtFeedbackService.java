package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Court;
import com.example.BE_SportCourtBooking.entity.CourtFeedback;
import com.example.BE_SportCourtBooking.exception.ResourceNotFoundException;
import com.example.BE_SportCourtBooking.model.Request.FeedbackRequest;
import com.example.BE_SportCourtBooking.model.Response.AccountResponse;
import com.example.BE_SportCourtBooking.model.Response.CourtFeedbackResponse;
import com.example.BE_SportCourtBooking.repository.AccountRepository;
import com.example.BE_SportCourtBooking.repository.CourtFeedbackRepository;
import com.example.BE_SportCourtBooking.repository.CourtRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourtFeedbackService {

    CourtFeedbackRepository feedbackRepository;

    CourtRepository courtRepository;

    AccountRepository accountRepository;

    private final CourtFeedbackRepository courtFeedbackRepository;

    public CourtFeedback createFeedback(FeedbackRequest request) {
        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found"));

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        CourtFeedback feedback = new CourtFeedback();
        feedback.setCourt(court);
        feedback.setAccount(account);
        feedback.setComment(request.getComment());
        feedback.setCourtQualityRating(request.getCourtQualityRating());
        feedback.setCleanlinessRating(request.getCleanlinessRating());
        feedback.setBookingExperienceRating(request.getBookingExperienceRating());
        feedback.setPlayedDate(request.getPlayedDate());

        // auto tính overallRating
        int total = request.getCourtQualityRating()
                + request.getCleanlinessRating()
                + request.getBookingExperienceRating();
        double average = total / 3.0;
        feedback.setOverallRating((float) average);

        return feedbackRepository.save(feedback);
    }

    public List<CourtFeedbackResponse> getFeedbacksByCourt(UUID courtId) {
        return feedbackRepository.findByCourt_Id(courtId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<CourtFeedbackResponse> getAllFeedbacks() {
        List<CourtFeedback> feedbackList = courtFeedbackRepository.findAll();
        return feedbackList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
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

    public List<CourtFeedbackResponse> getRandomFeedbacks(int limit) {
        List<CourtFeedback> allFeedbacks = courtFeedbackRepository.findAll();
        Collections.shuffle(allFeedbacks); // Random
        return allFeedbacks.stream()
                .limit(limit)
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

        Account account = feedback.getAccount();
        if (account != null) {
            AccountResponse accountResponse = new AccountResponse();
            accountResponse.setFullName(account.getFullName());
            accountResponse.setEmail(account.getEmail());
            dto.setAccount(accountResponse);
        }
        return dto;
    }


    public void deleteFeedbackById(UUID feedbackId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Account currentUser = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        CourtFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));

        boolean isOwner = feedback.getAccount().getId().equals(currentUser.getId());
        boolean isManagerOrStaff = currentUser.getRole().name().equals("MANAGER") || currentUser.getRole().name().equals("STAFF");

        if (!isOwner && !isManagerOrStaff) {
            throw new SecurityException("You are not allowed to delete this feedback");
        }

        feedbackRepository.deleteById(feedbackId);
    }

}
