package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.*;
import com.example.BE_SportCourtBooking.entity.Enum.*;
import com.example.BE_SportCourtBooking.model.Request.SlotRequest;
import com.example.BE_SportCourtBooking.model.Response.*;
import com.example.BE_SportCourtBooking.repository.AccountRepository;
import com.example.BE_SportCourtBooking.repository.CourtRepository;
import com.example.BE_SportCourtBooking.repository.PaymentRepository;
import com.example.BE_SportCourtBooking.repository.SlotRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    EmailService emailService;


    @Transactional
    public SlotResponse createSlot(SlotRequest slotRequest) {
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

        System.out.println("Checking overlap: courtId=" + court.getId() +
                ", slotType=" + slotRequest.getSlotType() +
                ", startDate=" + slotRequest.getStartDate() +
                ", endDate=" + slotRequest.getEndDate() +
                ", startTime=" + slotRequest.getStartTime() +
                ", endTime=" + slotRequest.getEndTime());
        long  isConflict = slotRepository.countOverlappingSlots(
                court.getId(),slotRequest.getSlotType(),slotRequest.getStartDate(), slotRequest.getEndDate(),
                slotRequest.getStartTime()+ ":00", slotRequest.getEndTime()+ ":00"

        );
        if(isConflict > 0) {
            throw new IllegalArgumentException("Slot overlaps with an existing slot");
        }

        Slot slot = new Slot();
        slot.setAccount(account);
        slot.setCourt(court);
        slot.setSlotType(slotRequest.getSlotType());
        slot.setStatus(SlotStatus.BOOKED);
        slot.setBookingStatus(BookingStatus.PENDING);
        slot.setStartDate(slotRequest.getStartDate());
        slot.setEndDate(slotRequest.getEndDate());
        slot.setPaymentMethod(slotRequest.getPaymentMethod());
        slot.setCreateAt(new Date());

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

            return modelMapper.map(slotRepository.save(slot), SlotResponse.class);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format for openTime or closeTime. Use HH:mm or HH:mm:ss.");
        }
    }

//    public Page<Slot> getAllSlot(PriceType slotType, SlotStatus slotStatus,Boolean isDelete, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        return slotRepository.findByFilters(slotType, slotStatus, isDelete, pageable);
//    }

    public Page<SlotDTO> getAllSlot(PriceType slotType, SlotStatus slotStatus, Boolean isDelete, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Slot> slotPage = slotRepository.findByFilters(slotType, slotStatus, isDelete, pageable);

        return slotPage.map(slot -> {
            SlotDTO dto = new SlotDTO();
            dto.setId(slot.getId());
            dto.setAccountUsername(slot.getAccount() != null ? slot.getAccount().getFullName() : null);
            dto.setAccountId(slot.getAccount() != null ? slot.getAccount().getId() : null);
            dto.setCourtName(slot.getCourt() != null ? slot.getCourt().getCourtName() : null);
            dto.setOwnerId(slot.getCourt() != null ? slot.getCourt().getCourtManager().getId() : null);
            dto.setSlotType(slot.getSlotType());
            dto.setStatus(slot.getStatus());
            dto.setStartDate(slot.getStartDate());
            dto.setEndDate(slot.getEndDate());
            dto.setStartTime(slot.getStartTime());
            dto.setEndTime(slot.getEndTime());
            dto.setPrice(slot.getPayment() != null ? slot.getPayment().getAmount() : null);
            return dto;
        });
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
        return modelMapper.map(slotRepository.save(slot), SlotResponse.class);
    }

    public void deleteSlot(UUID slotID) {
        Slot slot = slotRepository.findSlotById(slotID);
        if (slot == null) {
            throw new EntityNotFoundException("Slot not found");
        }
        if (slot.getStatus() == SlotStatus.CHECKED_IN || slot.getStatus() == SlotStatus.IN_USE) {
            throw new IllegalStateException("Cannot delete a checked-in slot or a slot that is currently in use");
        }
        slot.setIsDelete(true);
        slotRepository.save(slot);
    }

    public Slot updateSlot(UUID slotID, SlotRequest slotRequest) {
        Slot slot = slotRepository.findSlotById(slotID);
        if (slot == null) {
            throw new EntityNotFoundException("Slot not found");
        }
        if (slot.getStatus() != SlotStatus.BOOKED) {
            throw new IllegalArgumentException("Only booked slots can be updated");
        }
        if(slotRequest.getEndDate().isBefore(slotRequest.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date!");
        }
        if(slotRequest.getSlotType() == PriceType.HOURLY && slotRequest.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("\n"+ "Đặt sân theo giờ phải ở trong tương lai!");
        }
        long  isConflict = slotRepository.countOverlappingSlots(
                slot.getCourt().getId(),slotRequest.getSlotType(),slotRequest.getStartDate(), slotRequest.getEndDate(),
                slotRequest.getStartTime(), slotRequest.getEndTime()
        );
        if(isConflict > 0) {
            throw new IllegalArgumentException("Slot overlaps with an existing slot");
        }

        slot.setStartDate(slotRequest.getStartDate());
        slot.setEndDate(slotRequest.getEndDate());
        slot.setStartTime(slotRequest.getStartTime());
        slot.setEndTime(slotRequest.getEndTime());
        slot.setSlotType(slotRequest.getSlotType());
        slot.setPaymentMethod(slotRequest.getPaymentMethod());

        return slotRepository.save(slot);
    }



    @Transactional
    @Scheduled(fixedRate = 5 * 60 * 1000) // chạy mỗi 5 phút
    public void updateOverdueSlots() {
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        List<Slot> overdueSlots = slotRepository.findOverdueSlots(Timestamp.valueOf(fifteenMinutesAgo));

        for (Slot slot : overdueSlots) {
            slot.setBookingStatus(BookingStatus.OVERDUE);
            slot.setStatus(SlotStatus.CANCELED);
        }
        slotRepository.saveAll(overdueSlots);
    }

    @Scheduled(fixedRate = 60000) // mỗi phút
    public void checkAndSendReminders() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        LocalTime now = LocalTime.now();
        LocalTime threshold = now.plusMinutes(15);

        List<Slot> allBookings = slotRepository.findAll(); // Giả sử đây là danh sách cần check

        for (Slot booking : allBookings) {
            try {
                LocalTime bookingTime = LocalTime.parse(booking.getStartTime(), timeFormatter);


                if (!bookingTime.isBefore(now) && !bookingTime.isAfter(threshold)&& !booking.getReminderSent()) {
                    // Gửi mail nhắc nhở
                    emailService.sendReminderEmail(booking);
                    booking.setReminderSent(true);
                    slotRepository.save(booking);
                }

            } catch (DateTimeParseException e) {
                System.err.println("Lỗi định dạng thời gian: " + booking.getStartTime());
            }
        }
    }

    @Transactional
    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    public void updateFinishedSlotsAndSendThankYouEmail() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String currentTimeStr = now.format(timeFormatter);

        // Tìm các slot đã kết thúc và chưa hoàn tất
        List<Slot> finishedSlots = slotRepository.findFinishedSlots(
                SlotStatus.IN_USE,
                now.toLocalDate(),
                currentTimeStr
        );

        for (Slot slot : finishedSlots) {
            try {
                LocalDate endDate = slot.getEndDate();
                LocalTime endTime = LocalTime.parse(
                        slot.getEndTime().length() == 5 ? slot.getEndTime() + ":00" : slot.getEndTime(),
                        timeFormatter
                );
                LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

                if (now.isAfter(endDateTime) && !slot.getReminderSent()) {
                    slot.setStatus(SlotStatus.COMPLETED);
                    slot.setBookingStatus(BookingStatus.COMPLETED);
                    Slot updatedSlot = slotRepository.save(slot);
                    slotRepository.flush();

                    // Gửi email cảm ơn
                    emailService.sendThankYouEmail(slot);
                    updatedSlot.setReminderSent(true);
                    slotRepository.save(updatedSlot);
                    slotRepository.flush();

                    Court court = slot.getCourt();
                    if (court != null && court.getStatus() == CourtStatus.IN_USE) {
                        court.setStatus(CourtStatus.AVAILABLE);
                        courtRepository.save(court);
                        courtRepository.flush();
                    }
                }
            } catch (DateTimeParseException e) {
                System.err.println("Lỗi định dạng thời gian cho slot " + slot.getId() + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Lỗi khi xử lý slot " + slot.getId() + ": " + e.getMessage());
            }
        }
    }

    @Transactional
    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    public void updateCheckedInSlotsToInUse() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String currentTimeStr = now.format(timeFormatter);

        // Tìm các slot đã check-in và đến thời gian sử dụng
        List<Slot> checkedInSlots = slotRepository.findCheckedInSlots(
                SlotStatus.CHECKED_IN,
                now.toLocalDate(),
                currentTimeStr
        );

        for (Slot slot : checkedInSlots) {
            try {
                LocalDate startDate = slot.getStartDate();
                LocalTime startTime = LocalTime.parse(
                        slot.getStartTime().length() == 5 ? slot.getStartTime() + ":00" : slot.getStartTime(),
                        timeFormatter
                );
                LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);

                if (now.isAfter(startDateTime) || now.isEqual(startDateTime)) {
                    // Cập nhật trạng thái slot thành IN_USE
                    slot.setStatus(SlotStatus.IN_USE);
                    slot.setReminderSent(false);
                    slotRepository.save(slot);

                    // Cập nhật trạng thái court thành IN_USE
                    Court court = slot.getCourt();
                    if (court != null) {
                        court.setStatus(CourtStatus.IN_USE);
                        courtRepository.save(court);
                    }
                }
            } catch (DateTimeParseException e) {
                System.err.println("Lỗi định dạng thời gian cho slot " + slot.getId() + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Lỗi khi xử lý slot " + slot.getId() + ": " + e.getMessage());
            }
        }
    }

    public BookingHistoryResponse getBookingHistory(UUID accountId) {
        List<BookingResponse> bookingResponses = slotRepository.findBookingResponsesByAccountId(accountId);

        int totalBooking = bookingResponses.size();

        // Tính tổng chi tiêu từ price trong BookingResponse
        BigDecimal totalSpending = bookingResponses.stream()
                .map(BookingResponse::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Đếm loại sân yêu thích
        Map<CourtType, Integer> courtTypeCount = new HashMap<>();
        for (BookingResponse booking : bookingResponses) {
            if (booking.getCourt() != null && booking.getCourt().getCourtType() != null) {
                CourtType courtType = booking.getCourt().getCourtType();
                courtTypeCount.put(courtType, courtTypeCount.getOrDefault(courtType, 0) + 1);
            }
        }

        // Lấy tất cả các sân trong một truy vấn duy nhất
        List<UUID> courtIds = bookingResponses.stream()
                .map(BookingResponse::getCourt)
                .filter(Objects::nonNull)
                .map(CourtResponse::getId)
                .distinct()
                .collect(Collectors.toList());
        List<Court> courts = courtIds.isEmpty() ? Collections.emptyList() : courtRepository.findAllById(courtIds);

        // Tạo map để tra cứu nhanh
        Map<UUID, Court> courtMap = courts.stream()
                .collect(Collectors.toMap(Court::getId, Function.identity()));

        // Cập nhật prices và images cho mỗi CourtResponse
        for (BookingResponse br : bookingResponses) {
            if (br.getCourt() != null) {
                CourtResponse court = br.getCourt();
                Court fullCourt = courtMap.get(court.getId());
                if (fullCourt != null) {
                    List<CourtResponse.PriceResponse> priceResponses = fullCourt.getPrices().stream()
                            .map(p -> new CourtResponse.PriceResponse())
                            .collect(Collectors.toList());
                    court.setPrices(priceResponses);
                    court.setImages(fullCourt.getImages());
                }
            }
        }

        // Xác định loại sân yêu thích
        CourtType favoriteCourtType = null;
        int maxCount = 0;
        for (Map.Entry<CourtType, Integer> entry : courtTypeCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                favoriteCourtType = entry.getKey();
            }
        }

        return new BookingHistoryResponse(bookingResponses, totalBooking, totalSpending, favoriteCourtType);
    }
}
