package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.*;
import com.example.BE_SportCourtBooking.entity.Enum.*;
import com.example.BE_SportCourtBooking.model.Request.SlotRequest;
import com.example.BE_SportCourtBooking.model.Response.SlotResponse;
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
    public String createSlot(SlotRequest slotRequest) {
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
        slot.setBookingStatus(BookingStatus.PENDING);
        slot.setStartDate(slotRequest.getStartDate());
        slot.setEndDate(slotRequest.getEndDate());
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

            BigDecimal  price = court.getPrices().stream()
                    .filter(p -> p.getPriceType() == slotRequest.getSlotType())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Court does not have a price for the selected slot type"))
                    .getPrice();

            long durationUnits = 0;
            LocalDate startDate = slotRequest.getStartDate();
            LocalDate endDate = slotRequest.getEndDate();

            switch (slotRequest.getSlotType()) {
                case HOURLY:
                    long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                    long hoursPerDay = ChronoUnit.HOURS.between(openLocalTime, closeLocalTime);
                    durationUnits = totalDays * hoursPerDay;
                    break;

                case DAILY:
                    durationUnits = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                    break;

                case WEEKLY:
                    durationUnits = ChronoUnit.WEEKS.between(startDate, endDate) + 1;
                    break;

                case MONTHLY:
                    durationUnits = ChronoUnit.MONTHS.between(
                            startDate.withDayOfMonth(1),
                            endDate.withDayOfMonth(1)
                    ) + 1;
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported slot type: " + slotRequest.getSlotType());
            }

            BigDecimal  totalPrice = price.multiply(BigDecimal.valueOf(durationUnits));
            slot.setPrice(totalPrice);

            Slot createdSlot = slotRepository.save(slot);
            Payment payment = new Payment();
            payment.setSlot(slot);
            payment.setAmount(totalPrice);
            payment.setType(PaymentType.BANKING);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setVnpayTransactionId(UUID.randomUUID().toString());
            payment.setCreateAt(new Date());
            paymentRepository.save(payment);

            try {
                return createUrl(createdSlot);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format for openTime or closeTime. Use HH:mm or HH:mm:ss.");
        }
    }

    public Page<Slot> getAllSlot(PriceType slotType, SlotStatus slotStatus,Boolean isDelete, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return slotRepository.findByFilters(slotType, slotStatus, isDelete, pageable);
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


    public String createUrl(Slot slot) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createDate = LocalDateTime.now();
        String formattedCreateDate = createDate.format(formatter);

        //code minh`
        BigDecimal amount = slot.getPrice().multiply(new BigDecimal("100"));//khử thập phân theo VNPay's requirement
        String amountStr = amount.setScale(0, RoundingMode.HALF_UP).toPlainString();//ép về định dạng 0 dấu thập phân và ép về string để xóa dấu . thập phân

        String tmnCode = "4OBLXBGN"; // Your VNPay TmnCode
        String secretKey ="GJ8L3JFZNEC4ICPDZUMGJKKN2H5WORXK"; // Your VNPay secret key
        String vnpUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        String returnUrl = "http://localhost:5173/success?bookingID=" + slot.getId();//trang thong bao thanh cong o Front End
        String currCode = "VND";

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", currCode);
        vnpParams.put("vnp_TxnRef", slot.getId().toString());
        vnpParams.put("vnp_OrderInfo", "Thanh toan cho ma GD: " + slot.getId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Amount", amountStr);

        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_CreateDate", formattedCreateDate);
        vnpParams.put("vnp_IpAddr", "128.199.178.23");

        StringBuilder signDataBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            signDataBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
            signDataBuilder.append("=");
            signDataBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            signDataBuilder.append("&");
        }
        signDataBuilder.deleteCharAt(signDataBuilder.length() - 1); // Remove last '&'

        String signData = signDataBuilder.toString();
        String signed = generateHMAC(secretKey, signData);

        vnpParams.put("vnp_SecureHash", signed);

        StringBuilder urlBuilder = new StringBuilder(vnpUrl);
        urlBuilder.append("?");
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
            urlBuilder.append("=");
            urlBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            urlBuilder.append("&");
        }
        urlBuilder.deleteCharAt(urlBuilder.length() - 1); // Remove last '&'

        return urlBuilder.toString();
    }

    private String generateHMAC(String secretKey, String signData) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmacSha512.init(keySpec);
        byte[] hmacBytes = hmacSha512.doFinal(signData.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();
        for (byte b : hmacBytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public void createTransaction(UUID uuid) {
        // Find the order
        Slot slot = slotRepository.findSlotById(uuid);
        if (slot == null) {
            throw new EntityNotFoundException("Slot not found with ID: " + uuid);
        }
        Payment payment = paymentRepository.findBySlotId(uuid);
        if (payment == null) {
            throw new EntityNotFoundException("Payment not found for slot: " + uuid);
        }
        slot.setBookingStatus(BookingStatus.PAID);
        slotRepository.save(slot);
        payment.setStatus(PaymentStatus.COMPLETED);
        Set<Transaction> transactionSet = new HashSet<>();

        BigDecimal totalAmount = slot.getPrice();
        BigDecimal commission = totalAmount.multiply(new BigDecimal("0.10")); // 10% commission
        BigDecimal managerAmount = totalAmount.multiply(new BigDecimal("0.90")); // 90% to manager

        Account customer = slot.getAccount();
        if (customer == null) {
            throw new EntityNotFoundException("Customer account not found for slot: " + uuid);
        }
        Account admin = accountRepository.findAccountByRole(Role.ADMIN);
             if (admin == null){
                 throw new EntityNotFoundException("Platform admin account not found");
             }
        Account manager = slot.getCourt().getCourtManager();
        if (manager == null) {
            throw new EntityNotFoundException("Court manager account not found for slot: " + uuid);
        }

        // Create transaction 1
        Transaction transaction1 = new Transaction();
        transaction1.setPayment(payment);
        transaction1.setFrom(null); // From VNPay
        transaction1.setTo(customer);
        transaction1.setAmount(totalAmount);
        transaction1.setTransactionDate(new Date());
        transaction1.setStatus(TransactionEnum.SUCCESS);
        transaction1.setDescription("Payment from VNPay to customer for slot: " + uuid);

        // Add to transaction set
        transactionSet.add(transaction1);

        // Create transaction 2
        Transaction transaction2 = new Transaction();
        transaction2.setTransactionDate(new Date());
        transaction2.setFrom(customer);
        transaction2.setTo(admin);
        transaction2.setPayment(payment);
        transaction2.setAmount(totalAmount);
        transaction2.setStatus(TransactionEnum.SUCCESS);
        transaction2.setDescription("Payment from customer to platform for slot: " + uuid);

        transactionSet.add(transaction2);

        // Create transaction 3
        Transaction transaction3 = new Transaction();
        transaction3.setPayment(payment);
        transaction3.setFrom(admin);
        transaction3.setTo(manager);
        transaction3.setAmount(managerAmount);
        transaction3.setTransactionDate(new Date());
        transaction3.setStatus(TransactionEnum.SUCCESS);
        transaction3.setDescription("Payment from platform to manager (90%) for slot: " + uuid);


        // Add to transaction set
        transactionSet.add(transaction3);

        // Update balances
        admin.setBalance(admin.getBalance().add(commission));
        manager.setBalance(manager.getBalance().add(managerAmount));
        // Save transactions and payment
        payment.setTransactions(transactionSet);
        try {
            accountRepository.save(admin);
            accountRepository.save(manager);
            paymentRepository.save(payment); // Cascades to save transactions
        } catch (Exception e) {
            System.err.println("Error saving payment or transactions: " + e.getMessage());
            throw new RuntimeException("Failed to save payment or transactions", e);
        }
        emailService.sendBookingConfirmationEmail(slot);
        // Debugging output
        System.out.println("Transactions to be saved: " + payment.getTransactions());
    }

    @Transactional
    @Scheduled(fixedRate = 5 * 60 * 1000) // chạy mỗi 5 phút
    public void updateOverdueSlots() {
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        List<Slot> overdueSlots = slotRepository.findOverdueSlots(Timestamp.valueOf(fifteenMinutesAgo));

        for (Slot slot : overdueSlots) {
            slot.setBookingStatus(BookingStatus.OVERDUE);
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
}
