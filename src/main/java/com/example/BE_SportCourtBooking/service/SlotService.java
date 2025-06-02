package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.Court;
import com.example.BE_SportCourtBooking.entity.Enum.PriceType;
import com.example.BE_SportCourtBooking.entity.Enum.SlotStatus;
import com.example.BE_SportCourtBooking.entity.Slot;
import com.example.BE_SportCourtBooking.model.Request.SlotRequest;
import com.example.BE_SportCourtBooking.model.Response.SlotResponse;
import com.example.BE_SportCourtBooking.repository.AccountRepository;
import com.example.BE_SportCourtBooking.repository.CourtRepository;
import com.example.BE_SportCourtBooking.repository.SlotRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Service
public class SlotService {
    @Autowired
    ModelMapper modelMapper;

    @Autowired
    CourtRepository courtRepository;

    @Autowired
    SlotRepository slotRepository;

    @Autowired
    AccountRepository accountRepository;

    @Transactional
    public void createSlot(SlotRequest slotRequest) {
        Account account = accountRepository.findAccountById(slotRequest.getAccount());
        if (account == null) {
            throw new EntityNotFoundException("Account not found");
        }
        Court court = courtRepository.findCourtById(slotRequest.getCourt());
        if (court == null) {
            throw new EntityNotFoundException("Court not found");
        }
        if(slotRequest.getEndDate().isBefore(slotRequest.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date!");
        }
        if(slotRequest.getSlotType() == PriceType.HOURLY && slotRequest.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("\n"+ "Đặt sân theo giờ phải ở trong tương lai!");
        }
        boolean isConflict = slotRepository.countOverlappingSlots(
                court.getId(),slotRequest.getSlotType(),slotRequest.getStartDate(), slotRequest.getEndDate(),
                slotRequest.getStartTime(), slotRequest.getEndTime()
        );
        if(isConflict) {
            throw new IllegalArgumentException("Slot overlaps with an existing slot");
        }
        Slot slot = new Slot();
        slot.setAccount(account);
        slot.setCourt(court);
        slot.setSlotType(slotRequest.getSlotType());
        slot.setStatus(SlotStatus.BOOKED);
        slot.setStartDate(slotRequest.getStartDate());
        slot.setEndDate(slotRequest.getEndDate());

        try {
            String openTimeStr = slotRequest.getStartTime().length() == 5 ? slotRequest.getStartTime() + ":00" : slotRequest.getStartTime();
            String closeTimeStr = slotRequest.getEndTime().length() == 5 ? slotRequest.getEndTime() + ":00" : slotRequest.getEndTime();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime openLocalTime = LocalTime.parse(openTimeStr, timeFormatter);
            LocalTime closeLocalTime = LocalTime.parse(closeTimeStr, timeFormatter);
            if (!openLocalTime.isBefore(closeLocalTime)) {
                throw new IllegalArgumentException("Open time must be before close time!");
            }
            slot.setStartTime(String.valueOf(Time.valueOf(openLocalTime)));
            slot.setEndTime(String.valueOf(Time.valueOf(closeLocalTime)));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format for openTime or closeTime. Use HH:mm or HH:mm:ss.");
        }
        slotRepository.save(slot);
    }

    public Page<Slot> getAllSlot(PriceType slotType, SlotStatus slotStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return slotRepository.findByFilters(slotType, slotStatus, pageable);
    }

    public SlotResponse getSlot(UUID slotID) {
        Slot slot = slotRepository.findSlotById(slotID);
        if(slot == null ) throw new EntityNotFoundException("Slot not found");
        return modelMapper.map(slot, SlotResponse.class);
    }
    @Transactional
    public SlotResponse checkIn(UUID slotID) {
        Slot slot = slotRepository.findSlotById(slotID);
        if(slot == null ) throw new EntityNotFoundException("Slot not found");
        if(slot.getStatus() != SlotStatus.BOOKED){
            throw new IllegalArgumentException("Slot is not booked, cannot check in");
        }
        slot.setStatus(SlotStatus.CHECKED_IN);
        return modelMapper.map(slot, SlotResponse.class);
    }

    public void deleteSlot(UUID slotID) {
        Slot slot = slotRepository.findSlotById(slotID);
        if (slot == null) {
            throw new EntityNotFoundException("Slot not found");
        }
        if (slot.getStatus() == SlotStatus.CHECKED_IN || slot.getStatus() == SlotStatus.IN_USE) {
            throw new IllegalStateException("Cannot delete a checked-in slot or a slot that is currently in use");
        }
        slotRepository.delete(slot);
    }


}
