package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.*;
import com.example.BE_SportCourtBooking.entity.Enum.CourtStatus;
import com.example.BE_SportCourtBooking.entity.Enum.CourtType;
import com.example.BE_SportCourtBooking.entity.Enum.Role;
import com.example.BE_SportCourtBooking.model.Request.*;
import com.example.BE_SportCourtBooking.model.Response.CourtResponse;
import com.example.BE_SportCourtBooking.repository.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourtService {

    @Autowired
    CourtRepository courtRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    BusinessLocationRepo businessLocationRepository;

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    private JavaMailSender javaMailSender;

    public CourtResponse createCourt(CourtRequest courtRequest) {
        try {
            if (courtRequest.getPrices() == null || courtRequest.getPrices().isEmpty()) {
                throw new IllegalArgumentException("Prices cannot be null or empty");
            }

            Optional<Account> optionalAccount = accountRepository.findById(courtRequest.getManager_id());
            Account account = optionalAccount.orElseThrow(() -> new EntityNotFoundException("Account not found"));
            if (account.getRole() != Role.ADMIN && account.getRole() != Role.MANAGER) {
                throw new IllegalArgumentException("Account is not a manager or admin");
            }

            BusinessLocation businessLocation = businessLocationRepository.findBusinessLocationById(courtRequest.getBusinessLocationId());
            if( businessLocation == null) {
                throw new EntityNotFoundException("Business Location not found");
            }
            Court court = new Court();
            court.setCourtType(courtRequest.getCourtType());
            court.setCourtName(courtRequest.getCourtName());
            court.setBusinessLocation(businessLocation);
            court.setYearBuild(courtRequest.getYearBuild());
            court.setLength(courtRequest.getLength());
            court.setWidth(courtRequest.getWidth());
            court.setMaxPlayers(courtRequest.getMaxPlayers());
            court.setDescription(courtRequest.getDescription());
            court.setCourtManager(account);
            court.setStatus(CourtStatus.AVAILABLE);

            List<CourtPricing> prices = courtRequest.getPrices().stream()
                    .map(priceRequest -> {
                        CourtPricing courtPricing = new CourtPricing();
                        courtPricing.setCourt(court);
                        courtPricing.setPriceType(priceRequest.getPriceType());
                        courtPricing.setPrice(priceRequest.getPrice());
                        return courtPricing;
                    }).toList();
            court.setPrices(prices);

                List<Image> images = courtRequest.getImages().stream()
                        .filter(imageUrl -> imageUrl != null && !imageUrl.trim().isEmpty()) // Filter out null or empty URLs
                        .map(imageUrl -> {
                            Image image = new Image();
                            image.setCourt(court);
                            image.setImageUrl(imageUrl);
                            return image;
                        })
                        .collect(Collectors.toList());
                court.setImages(images);


           return modelMapper.map(courtRepository.save(court), CourtResponse.class);
        }   catch (RuntimeException e) {
            e.printStackTrace();
            throw new EntityNotFoundException("Create court error", e);
        }
    }

    public Page<CourtResponse> getAllCourts(CourtType courtType, CourtStatus status, String courtName,Boolean isDelete, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Court> court = courtRepository.findByFilters(
                courtType,
                status,
                StringUtils.hasText(courtName) ? courtName : null,
                isDelete,
                pageable);
        return court.map(c -> modelMapper.map(c, CourtResponse.class));
    }

    @Transactional
    public CourtResponse updateCourtStatus(UUID courtID, CourtStatusRequest statusRequest) {
        Court court = courtRepository.findCourtById(courtID);
                if(court == null ) throw new EntityNotFoundException("Court not found");
                court.setStatus(statusRequest.getStatus());
        return modelMapper.map(court, CourtResponse.class);
    }

    public Court getCourt(UUID courtID) {
        Court court = courtRepository.findCourtById(courtID);
        if(court == null ) throw new EntityNotFoundException("Court not found");
        return court;
    }

    public void deleteCourt(UUID courtID){
        Court court = courtRepository.findCourtById(courtID);
        if(court == null) throw new RuntimeException("Court not found");
        if (court.getStatus() == CourtStatus.IN_USE ) {
            throw new IllegalStateException("Cannot delete court that is currently in use");
        }
        // Xóa toàn bộ hình ảnh liên quan đến sân
        List<Image> images = court.getImages();
        if (images != null && !images.isEmpty()) {
            imageRepository.deleteAll(images);
            court.getImages().clear();
        }
        court.setIsDelete(true);
        court.setStatus(CourtStatus.INACTIVE);
        courtRepository.save(court);
    }

    public CourtResponse updateCourt(UUID courtId, CourtUpdateRequest courtUpdateRequest) {
        Court court = courtRepository.findCourtById(courtId);
        if (court == null) {
            throw new EntityNotFoundException("Court not found");
        }
        court.setCourtType(courtUpdateRequest.getCourtType());
        court.setCourtName(courtUpdateRequest.getCourtName());
        court.setDescription(courtUpdateRequest.getDescription());
        court.setLength(courtUpdateRequest.getLength());
        court.setWidth(courtUpdateRequest.getWidth());
        court.setMaxPlayers(courtUpdateRequest.getMaxPlayers());
        court.setYearBuild(courtUpdateRequest.getYearBuild());
        Optional<Account> optionalAccount = accountRepository.findById(courtUpdateRequest.getManager_id());
        Account account = optionalAccount.orElseThrow(() -> new EntityNotFoundException("Account not found"));
        if (account.getRole() != Role.ADMIN && account.getRole() != Role.MANAGER) {
            throw new IllegalArgumentException("Account is not a manager or admin");
        }
        court.setCourtManager(account);
        BusinessLocation businessLocation = businessLocationRepository.findBusinessLocationById(courtUpdateRequest.getBusinessLocationId());
        if (businessLocation == null) {
            throw new EntityNotFoundException("Business Location not found");
        }
        court.setBusinessLocation(businessLocation);
//        if (courtRequest.getPrices() != null && !courtRequest.getPrices().isEmpty()) {
//            List<CourtPricing> prices = courtRequest.getPrices().stream()
//                    .map(priceRequest -> {
//                        CourtPricing courtPricing = new CourtPricing();
//                        courtPricing.setCourt(court);
//                        courtPricing.setPriceType(priceRequest.getPriceType());
//                        courtPricing.setPrice(priceRequest.getPrice());
//                        return courtPricing;
//                    }).toList();
//            court.setPrices(prices);
//        }
//        if (courtUpdateRequest.getImages() != null && !courtUpdateRequest.getImages().isEmpty()) {
//            List<Image> newImages = courtUpdateRequest.getImages().stream().map(url -> {
//                Image image = new Image();
//                image.setImageUrl(url);
//                image.setCourt(court);
//                return image;
//            }).collect(Collectors.toList());
//            court.getImages().addAll(newImages);
//        }
        return modelMapper.map(courtRepository.save(court), CourtResponse.class);
    }

    public CourtResponse updateCourtPrice(UUID courtId, List<CourtPricingRequest> newPrices) {
        Court court = courtRepository.findCourtById(courtId);
        if (court == null) {
            throw new EntityNotFoundException("Court not found");
        }
        if (newPrices == null || newPrices.isEmpty()) {
            throw new IllegalArgumentException("Prices cannot be null or empty");
        }
        List<CourtPricing> existingPrices = court.getPrices();
        for (CourtPricingRequest request : newPrices) {
            Optional<CourtPricing> existing = existingPrices.stream()
                    .filter(p -> p.getPriceType().equals(request.getPriceType()))
                    .findFirst();

            if (existing.isPresent()) {
                existing.get().setPrice(request.getPrice());
            } else {
                CourtPricing newPricing = new CourtPricing();
                newPricing.setCourt(court);
                newPricing.setPriceType(request.getPriceType());
                newPricing.setPrice(request.getPrice());
                existingPrices.add(newPricing);
            }
        }
        Court updatedCourt = courtRepository.save(court);
        return modelMapper.map(updatedCourt, CourtResponse.class);
    }

    public void deleteImage(UUID courtId, UUID imageId) {
        Court court = courtRepository.findCourtById(courtId);
        if (court == null) {
            throw new EntityNotFoundException("Court not found");
        }
        Image imageToDelete = court.getImages().stream()
                .filter(image -> image.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Image not found"));
        court.getImages().remove(imageToDelete);
        imageRepository.delete(imageToDelete);
        courtRepository.save(court);
    }

    public List<String> getImagesByCourt(UUID courtId) {
        Court court = courtRepository.findCourtById(courtId);
        if (court == null) {
            throw new EntityNotFoundException("Court not found");
        }

        return court.getImages().stream()
                .map(Image::getImageUrl)
                .collect(Collectors.toList());
    }

    public void addImagesToCourt(UUID courtId, List<String> newImageUrls) {
        Court court = courtRepository.findCourtById(courtId);
        if (court == null) {
            throw new EntityNotFoundException("Court not found");
        }

        List<Image> currentImages = court.getImages();

        for (String url : newImageUrls) {
            Image image = new Image();
            image.setId(UUID.randomUUID());
            image.setImageUrl(url);
            image.setCourt(court);
            currentImages.add(image);
        }
        courtRepository.save(court);
    }

    public Page<CourtResponse> getCourtsByBusinessLocation(UUID businessLocationId, Boolean isDelete, int page, int size) {
        BusinessLocation businessLocation = businessLocationRepository.findBusinessLocationById(businessLocationId);
        if (businessLocation == null) {
            throw new EntityNotFoundException("Business Location not found");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Court> courts = courtRepository.findCourtsByBusinessLocationId(businessLocationId, isDelete, pageable);

        return courts.map(court -> modelMapper.map(court, CourtResponse.class));
    }

    public List<CourtResponse> getTop3CourtsByBookingCount() {
        List<Object[]> results = courtRepository.findTop3CourtsByBookingCount();

        return results.stream()
                .limit(3) // Đảm bảo chỉ lấy 3 sân
                .map(result -> {
                    Court court = (Court) result[0];
                    CourtResponse response = new CourtResponse();
                    response.setId(court.getId());
                    response.setCourtType(court.getCourtType());
                    response.setCourtName(court.getCourtName());
                    response.setDescription(court.getDescription());
                    response.setStatus(court.getStatus());
                    response.setYearBuild(court.getYearBuild());
                    response.setLength(court.getLength());
                    response.setWidth(court.getWidth());
                    response.setMaxPlayers(court.getMaxPlayers());
                    response.setImages(court.getImages());
                    response.setBusinessLocation(court.getBusinessLocation());
                    response.setPrices(court.getPrices().stream()
                            .map(price -> {
                                CourtResponse.PriceResponse priceResponse = new CourtResponse.PriceResponse();
                                priceResponse.setPriceType(price.getPriceType());
                                priceResponse.setPrice(price.getPrice());
                                return priceResponse;
                            })
                            .collect(Collectors.toList()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    public List<Court> getCourtByOwner(UUID id) {
        Account owner = accountRepository.findAccountById(id);
        if(owner == null) {
            throw new EntityNotFoundException("Owner account not found");
        }
        List<Court>  courts = courtRepository.findCourtByOwner(id);
        if(courts.isEmpty()){
            throw new EntityNotFoundException("No business location found for this owner");
        }
        return courts;
    }

    @Transactional
    public Report sendReport(UUID staffId, ReportRequest reportRequest) {
        if (reportRequest.getBusinessLocation() == null || reportRequest.getCourt() == null || reportRequest.getContent() == null || reportRequest.getRecipientEmail() == null) {
            throw new IllegalArgumentException("All fields are required");
        }

        // Verify staff
        Account staff = accountRepository.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Staff account not found"));
        if (staff.getRole() != Role.STAFF) {
            throw new IllegalArgumentException("Account is not a staff member");
        }

        // Verify business location
        BusinessLocation businessLocation = businessLocationRepository.findBusinessLocationById(reportRequest.getBusinessLocation());
        if (businessLocation == null) {
            throw new EntityNotFoundException("Business Location not found");
        }

        // Verify court
        Court court = courtRepository.findCourtById(reportRequest.getCourt());
        if (court == null) {
            throw new EntityNotFoundException("Court not found");
        }

        // Verify staff is assigned to the court
        // Assuming court has a courtManager field
        if (!court.getBusinessLocation().getOwner().getId().equals(staff.getManagerId())) {
            System.out.println("Staff ID: " + staff.getId() + ", Manager ID: " + court.getBusinessLocation().getOwner().getId() + ",ManageStaff: " + staff.getManagerId());
            throw new IllegalArgumentException("Staff is not assigned to manage this court");
        }

        // Verify manager and email
        Account manager = businessLocation.getOwner();
//        if (manager == null || manager.getRole() != Role.MANAGER) {
//            throw new EntityNotFoundException("Manager not found for this business location");
//        }
        if (!reportRequest.getRecipientEmail().equals(manager.getEmail())) {
            throw new IllegalArgumentException("Recipient email must match the manager's email");
        }

        // Create report entity
        Report report = new Report();
        report.setBusinessLocation(businessLocation);
        report.setCourt(court);
        report.setContent(reportRequest.getContent());
        report.setRecipientEmail(reportRequest.getRecipientEmail());
        report.setStatus(Report.ReportStatus.GENERATED);
        report.setStaff(staff);
        report.setCreatedAt(new Date()); // Explicitly set creation time
        reportRepository.save(report);

        // Update court status to MAINTENANCE if applicable
        if (court.getStatus() != CourtStatus.MAINTENANCE) {
            court.setStatus(CourtStatus.MAINTENANCE);
            courtRepository.save(court);
        }

        // Send report email
        try {
            String htmlContent = "<html><body>" +
                    "<h3>Court Issue Report</h3>" +
                    "<p><strong>Court:</strong> " + court.getCourtName() + "</p>" +
                    "<p><strong>Location:</strong> " + court.getBusinessLocation().getName() + "</p>" +
                    "<p><strong>Reported by:</strong> " + staff.getFullName() + "</p>" +
                    "<p><strong>Content:</strong><br/>" + reportRequest.getContent() + "</p>" +
                    "</body></html>";

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(reportRequest.getRecipientEmail());
            helper.setSubject("Court Issue Report");
            helper.setText(htmlContent, true); // 'true' => HTML email

            javaMailSender.send(message);
            report.setStatus(Report.ReportStatus.SENT);
            reportRepository.save(report);
        } catch (RuntimeException e) {
            report.setStatus(Report.ReportStatus.FAILED);
            reportRepository.save(report);
            throw e;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        return report;
    }

    public List<Report> getReportsStaff(UUID staffId) {
        Account staff = accountRepository.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Staff account not found"));
        if (staff == null) {
            throw new EntityNotFoundException("taff not found");
        }
        return reportRepository.findReportByStaffId(staffId);
    }

    public List<Report> getAllReportByLocation(UUID businessLocationId) {
        BusinessLocation businessLocation = businessLocationRepository.findBusinessLocationById(businessLocationId);
        if (businessLocation == null) {
            throw new EntityNotFoundException("Business Location not found");
        }
        return reportRepository.findReportByBusinessLocationId(businessLocationId);
    }

}
