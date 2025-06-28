package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.*;
import com.example.BE_SportCourtBooking.entity.Enum.*;
import com.example.BE_SportCourtBooking.repository.AccountRepository;
import com.example.BE_SportCourtBooking.repository.PaymentRepository;
import com.example.BE_SportCourtBooking.repository.SlotRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class PaymentService {

    @Autowired
    SlotRepository slotRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    EmailService emailService;

    public String createUrl(UUID slotId) throws Exception {

        Slot slotData = slotRepository.findSlotById(slotId);
        if(slotData == null) {
            throw new EntityNotFoundException("Slot not found with ID: " + slotId);
        }
        if (slotData.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Slot is not in PENDING status");
        }
        //code minh`
        // Parse start and end times
        DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder()
                .appendPattern("HH:mm")
                .optionalStart()
                .appendPattern(":ss")
                .optionalEnd()
                .toFormatter();

        LocalDateTime start = LocalDateTime.of(slotData.getStartDate(),
                LocalTime.parse(slotData.getStartTime(), timeFormatter));
        LocalDateTime end = LocalDateTime.of(slotData.getEndDate(),
                LocalTime.parse(slotData.getEndTime(), timeFormatter));

        // Calculate duration
        Duration duration = Duration.between(start, end);
        if (duration.isNegative()) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        // Find matching CourtPricing
        CourtPricing courtPricing = slotData.getCourt().getPrices()
                .stream()
                .filter(p -> p.getPriceType() == slotData.getSlotType())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No pricing found for slot type: " + slotData.getSlotType()));
        // Calculate amount based on priceType
        BigDecimal amount;
        switch (courtPricing.getPriceType()) {
            case HOURLY:
                double hours = duration.toMinutes() / 60.0; // Convert to hours (e.g., 90 minutes = 1.5 hours)
                amount = courtPricing.getPrice().multiply(new BigDecimal(hours));
                break;
            case DAILY:
                long days = ChronoUnit.DAYS.between(slotData.getStartDate(), slotData.getEndDate()) + 1; // Inclusive
                amount = courtPricing.getPrice().multiply(new BigDecimal(days));
                break;
            case WEEKLY:
                long weeks = ChronoUnit.WEEKS.between(slotData.getStartDate(), slotData.getEndDate()) + 1;
                amount = courtPricing.getPrice().multiply(new BigDecimal(weeks));
                break;
            case MONTHLY:
                long months = ChronoUnit.MONTHS.between(slotData.getStartDate(), slotData.getEndDate()) + 1;
                amount = courtPricing.getPrice().multiply(new BigDecimal(months));
                break;
            default:
                throw new IllegalArgumentException("Unsupported price type: " + courtPricing.getPriceType());
        }

        Optional<Payment> existingPayment = paymentRepository.findBySlotId(slotId);
        if (existingPayment.isPresent() && existingPayment.get().getStatus() == PaymentStatus.PENDING) {
            // Reuse existing payment
        } else {
            Payment payment = new Payment();
            payment.setSlot(slotData);
            BigDecimal amountVND = amount.setScale(0, RoundingMode.HALF_UP); // lưu DB
            payment.setAmount(amountVND); // Store actual amount in VND
            payment.setType(PaymentType.BANKING);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setCreateAt(new Date());
            paymentRepository.save(payment);
            existingPayment = Optional.of(payment);
        }


        // Convert amount for VNPay (multiply by 100, remove decimals)
        BigDecimal vnpayAmount = amount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP);
        String amountStr = vnpayAmount.toPlainString();
//        BigDecimal amount = slot.getCourt().getPrices().multiply(new BigDecimal("100"));//khử thập phân theo VNPay's requirement
//        String amountStr = amount.setScale(0, RoundingMode.HALF_UP).toPlainString();//ép về định dạng 0 dấu thập phân và ép về string để xóa dấu . thập phân

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createDate = LocalDateTime.now();
        String formattedCreateDate = createDate.format(formatter);

        String tmnCode = "4OBLXBGN"; // Your VNPay TmnCode
        String secretKey ="GJ8L3JFZNEC4ICPDZUMGJKKN2H5WORXK"; // Your VNPay secret key
        String vnpUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        String returnUrl = "http://localhost:5173/?bookingID=" + slotData.getId();//trang thong bao thanh cong o Front End
        String currCode = "VND";

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", currCode);
        vnpParams.put("vnp_TxnRef", slotData.getId().toString());
        vnpParams.put("vnp_OrderInfo", "Thanh toan cho ma GD: " + slotData.getId());
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
        System.out.println("Slot: " + slot);
        Optional<Payment> paymentOptional = paymentRepository.findBySlotId(uuid);
        System.out.println("Payment Optional: " + paymentOptional);
        if (!paymentOptional.isPresent()) throw new EntityNotFoundException("Payment not found for slot: " + uuid);
        Payment payment = paymentOptional.get();

        if (slot.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Slot is not in PENDING status");
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not in PENDING status");
        }
        BigDecimal totalAmount = payment.getAmount();
        slot.setBookingStatus(BookingStatus.PAID);
//        slotRepository.save(slot);
        payment.setStatus(PaymentStatus.COMPLETED);
        Set<Transaction> transactionSet = new HashSet<>();


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
            slotRepository.save(slot);
            accountRepository.save(admin);
            accountRepository.save(manager);
            paymentRepository.save(payment); // Cascades to save transactions
        } catch (Exception e) {
            System.err.println("Error saving payment or transactions: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save payment or transactions", e);
        }
        emailService.sendBookingConfirmationEmail(slot);
        // Debugging output
        System.out.println("Transactions to be saved: " + payment.getTransactions());
    }

}
