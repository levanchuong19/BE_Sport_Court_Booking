package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.Account;
import com.example.BE_SportCourtBooking.entity.BusinessLocation;
import com.example.BE_SportCourtBooking.entity.Court;
import com.example.BE_SportCourtBooking.entity.Enum.CourtStatus;
import com.example.BE_SportCourtBooking.entity.Enum.LocationStatus;
import com.example.BE_SportCourtBooking.entity.Enum.NotificationType;
import com.example.BE_SportCourtBooking.entity.Enum.Role;
import com.example.BE_SportCourtBooking.model.Request.BusinessLocationRequest;
import com.example.BE_SportCourtBooking.model.Response.AccountResponse;
import com.example.BE_SportCourtBooking.model.Response.BusinessLocationResponse;
import com.example.BE_SportCourtBooking.model.Response.CourtResponse;
import com.example.BE_SportCourtBooking.repository.AccountRepository;
import com.example.BE_SportCourtBooking.repository.BusinessLocationRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BusinessLocationService {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    BusinessLocationRepo businessLocationRepo;

    @Autowired
    NotificationService notificationService;

    @Autowired
    ModelMapper modelMapper;

    public void createBusinessLocation(BusinessLocationRequest request) {
        Account account = accountRepository.findAccountById(request.getOwner());
        if (account == null) {
            throw new IllegalArgumentException("Owner account not found");
        }
        if (account.getRole() == Role.CUSTOMER) {
            account.setRole(Role.MANAGER); // Promote to manager
            accountRepository.save(account); // Save updated role
        } else if (account.getRole() != Role.MANAGER && account.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only managers or admins can create a business location");
        }
        BusinessLocation businessLocation = new BusinessLocation();
        businessLocation.setName(request.getName());
        businessLocation.setAddress(request.getAddress());
        businessLocation.setImages(request.getImages());
        businessLocation.setDescription(request.getDescription());
        businessLocation.setStatus(LocationStatus.INACTIVE);
        businessLocation.setCreateAt(new java.util.Date());
        businessLocation.setCourtNum(request.getCourtNum());
        businessLocation.setYearBuild(request.getYearBuild());
        businessLocation.setUtilities(request.getUtilities());
        businessLocation.setLatitude(request.getLatitude());
        businessLocation.setLongitude(request.getLongitude());
        businessLocation.setBusinessLicense(request.getBusinessLicense());
        businessLocation.setOwner(account);
        try {
            // Chuẩn hóa định dạng: thêm ":00" nếu chỉ có HH:mm
            String openTimeStr = request.getOpenTime().length() == 5 ? request.getOpenTime() + ":00" : request.getOpenTime();
            String closeTimeStr = request.getCloseTime().length() == 5 ? request.getCloseTime() + ":00" : request.getCloseTime();

            // Parse chuỗi thành LocalTime
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime openLocalTime = LocalTime.parse(openTimeStr, timeFormatter);
            LocalTime closeLocalTime = LocalTime.parse(closeTimeStr, timeFormatter);

            // Kiểm tra openTime < closeTime
            if (!openLocalTime.isBefore(closeLocalTime)) {
                throw new IllegalArgumentException("Open time must be before close time!");
            }

            // Chuyển LocalTime sang java.sql.Time
            businessLocation.setOpenTime(String.valueOf(Time.valueOf(openLocalTime)));
            businessLocation.setCloseTime(String.valueOf(Time.valueOf(closeLocalTime)));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format for openTime or closeTime. Use HH:mm or HH:mm:ss.");
        }
        businessLocationRepo.save(businessLocation);
    }
    public List<BusinessLocationResponse> getAll() {
        List<BusinessLocation> businessLocations = businessLocationRepo.findAll();
        Type listType = new TypeToken<List<BusinessLocationResponse>>() {}.getType();
        return modelMapper.map(businessLocations, listType);
    }

    public BusinessLocation getById(UUID id) {
        BusinessLocation businessLocation = businessLocationRepo.findBusinessLocationById(id);
        if (businessLocation == null) {
            throw new IllegalArgumentException("Business Location not found");
        }
//        return modelMapper.map(businessLocation, BusinessLocationResponse.class);
        return businessLocation;
    }

    public Page<BusinessLocation> getAllWithPagination(String name,String address,Boolean isDelete, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return businessLocationRepo.findByFilters(
                name,
                address,
                isDelete,
                pageable);
    }

    public void deleteBusinessLocation(UUID id) {
        BusinessLocation businessLocation = businessLocationRepo.findBusinessLocationById(id);
        if (businessLocation == null) {
            throw new IllegalArgumentException("Business Location not found");
        }
        businessLocation.setIsDelete(true);
        businessLocation.setStatus(LocationStatus.DELETED);

        if(businessLocation.getCourts() != null) {
            for (Court court : businessLocation.getCourts()) {
                court.setIsDelete(true);
                court.setStatus(CourtStatus.INACTIVE);
            }
        }

        notificationService.sendNotification(
                businessLocation.getOwner(),
                "Địa điểm của bạn đã bị vô hiệu hoá",
                "Vui lòng liên hệ quản trị viên của hệ thống để xử lý.",
                NotificationType.SYSTEM,
                null
        );
        businessLocationRepo.save(businessLocation);
    }

    @Transactional
    public BusinessLocationResponse updateBusinessLocation(UUID id, BusinessLocationRequest request) {
        BusinessLocation businessLocation = businessLocationRepo.findBusinessLocationById(id);
        if (businessLocation == null) {
            throw new IllegalArgumentException("Business Location not found");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("Business Location name cannot be empty");
        }
        if (!StringUtils.hasText(request.getAddress())) {
            throw new IllegalArgumentException("Address cannot be empty");
        }
        businessLocation.setName(request.getName());
        businessLocation.setAddress(request.getAddress());
        try {
            // Chuẩn hóa định dạng: thêm ":00" nếu chỉ có HH:mm
            String openTimeStr = request.getOpenTime().length() == 5 ? request.getOpenTime() + ":00" : request.getOpenTime();
            String closeTimeStr = request.getCloseTime().length() == 5 ? request.getCloseTime() + ":00" : request.getCloseTime();

            // Parse chuỗi thành LocalTime
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime openLocalTime = LocalTime.parse(openTimeStr, timeFormatter);
            LocalTime closeLocalTime = LocalTime.parse(closeTimeStr, timeFormatter);

            // Kiểm tra openTime < closeTime
            if (!openLocalTime.isBefore(closeLocalTime)) {
                throw new IllegalArgumentException("Open time must be before close time!");
            }

            // Chuyển LocalTime sang java.sql.Time
            businessLocation.setOpenTime(String.valueOf(Time.valueOf(openLocalTime)));
            businessLocation.setCloseTime(String.valueOf(Time.valueOf(closeLocalTime)));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format for openTime or closeTime. Use HH:mm or HH:mm:ss.");
        }
        Optional<Account> optionalAccount = accountRepository.findById(request.getOwner());
        Account account = optionalAccount.orElseThrow(() -> new EntityNotFoundException("Account not found"));
        if (account.getRole() != Role.ADMIN && account.getRole() != Role.MANAGER) {
            throw new IllegalArgumentException("Account is not a manager or admin");
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            businessLocation.setImages(request.getImages());
        }
        businessLocation.setDescription(request.getDescription());
        businessLocation.setModifiedAt(new java.util.Date());
        businessLocation.setCourtNum(request.getCourtNum());
        businessLocation.setYearBuild(request.getYearBuild());
        businessLocation.setUtilities(request.getUtilities());
        businessLocation.setBusinessLicense(request.getBusinessLicense());
        businessLocation.setOwner(account);
        businessLocation.setLatitude(request.getLatitude());
        businessLocation.setLongitude(request.getLongitude());
        businessLocationRepo.save(businessLocation);

        return modelMapper.map(businessLocation, BusinessLocationResponse.class);
    }

    public Page<BusinessLocationResponse> getBusinessLocationsByOwnerId(UUID ownerId,Boolean isDelete, int page, int size) {
        Account owner = accountRepository.findAccountById(ownerId);
        if (owner == null) {
            throw new IllegalArgumentException("Owner account not found");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<BusinessLocation> businessLocations = businessLocationRepo.findBusinessLocationsByOwnerId(ownerId , isDelete, pageable);
        if (businessLocations.isEmpty()) {
            throw new IllegalArgumentException("No business locations found for this owner");
        }
        Type listType = new TypeToken<Page<BusinessLocationResponse>>() {}.getType();
        return modelMapper.map(businessLocations, listType);
    }

    public List<BusinessLocationResponse> getTop3BusinessLocationsByBookingCount() {
        List<Object[]> results = businessLocationRepo.findTop3BusinessLocationsByBookingCount(PageRequest.of(0, 3));
        return results.stream()
                .limit(3)
                .map(result -> {
            BusinessLocation businessLocation = (BusinessLocation) result[0];
            BusinessLocationResponse response = new BusinessLocationResponse();
            response.setId(businessLocation.getId());
            response.setName(businessLocation.getName());
            response.setAddress(businessLocation.getAddress());
            response.setOpenTime(businessLocation.getOpenTime());
            response.setCloseTime(businessLocation.getCloseTime());
            response.setDescription(businessLocation.getDescription());
            response.setImages(businessLocation.getImages());
            response.setCreateAt(businessLocation.getCreateAt());
            response.setModifiedAt(businessLocation.getModifiedAt());
            response.setCourtNum(businessLocation.getCourtNum());
            response.setYearBuild(businessLocation.getYearBuild());
            response.setUtilities(businessLocation.getUtilities());
            response.setBusinessLicense(businessLocation.getBusinessLicense());
            response.setStatus(businessLocation.getStatus());
            response.setLatitude(businessLocation.getLatitude());
            response.setLongitude(businessLocation.getLongitude());
            Account owner = businessLocation.getOwner();
            response.setOwner(modelMapper.map(owner, AccountResponse.class));
            response.setCourts(businessLocation.getCourts().stream()
                    .map(court -> modelMapper.map(court, CourtResponse.class))
                    .collect(Collectors.toSet()));
            return response;
        }).toList();
    }

    @Transactional
    public BusinessLocationResponse activeBusinessLocation(UUID id) {
        BusinessLocation businessLocation = businessLocationRepo.findBusinessLocationById(id);

        if (businessLocation == null) {
            throw new IllegalArgumentException("Business Location not found");
        }
        if (businessLocation.getStatus() != LocationStatus.INACTIVE) {
            throw new IllegalArgumentException("Business Location is not allowed to be active");
        }

        Account owner = businessLocation.getOwner();

        if(owner.getRole() == Role.CUSTOMER) {
            owner.setRole(Role.MANAGER);
            accountRepository.save(owner);
        }

        businessLocation.setStatus(LocationStatus.ACTIVE);
        businessLocationRepo.save(businessLocation);

        notificationService.sendNotification(
                businessLocation.getOwner(),
                "Địa điểm của bạn đã được duyệt",
                "Chúc mừng! Địa điểm của bạn đã được duyệt và hiện đã hoạt động.",
                NotificationType.SYSTEM,
                null
        );

        return modelMapper.map(businessLocation, BusinessLocationResponse.class);
    }

    public BusinessLocationResponse rejectBusinessLocation(UUID id, String reason) {
        BusinessLocation businessLocation = businessLocationRepo.findBusinessLocationById(id);
        if (businessLocation == null) {
            throw new IllegalArgumentException("Business Location not found");
        }
        if (businessLocation.getStatus() != LocationStatus.INACTIVE) {
            throw new IllegalArgumentException("Business Location is not allowed to be rejected");
        }
        businessLocation.setStatus(LocationStatus.REJECTED);
        businessLocationRepo.save(businessLocation);

        notificationService.sendNotification(
                businessLocation.getOwner(),
                "Địa điểm của bạn đã bị từ chối",
                "Lí do: " + reason,
                NotificationType.SYSTEM,
                null
        );

        return modelMapper.map(businessLocation, BusinessLocationResponse.class);
    }

    public List<BusinessLocation> getBusinessLocationByOwner(UUID id) {
        Account owner = accountRepository.findAccountById(id);
        if(owner == null) {
            throw new EntityNotFoundException("Owner account not found");
        }
         List<BusinessLocation>  locations = businessLocationRepo.findBusinessLocationByOwner(id);
                if(locations.isEmpty()){
                    throw new EntityNotFoundException("No business location found for this owner");
                }
                return locations;
    }

    public List<BusinessLocation> getUnactiveBusinessLocation() {
        return businessLocationRepo.findByStatus(LocationStatus.INACTIVE);
    }
}
    