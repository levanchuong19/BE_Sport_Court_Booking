package com.example.BE_SportCourtBooking.service;

import com.example.BE_SportCourtBooking.entity.BusinessLocation;
import com.example.BE_SportCourtBooking.entity.Field;
import com.example.BE_SportCourtBooking.model.Request.SearchRequest;
import com.example.BE_SportCourtBooking.model.Response.BusinessLocationResponse;
import com.example.BE_SportCourtBooking.repository.SearchRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    SearchRepository searchRepository;

    @Autowired
    ModelMapper modelMapper;

    private void calculateDistance(BusinessLocationResponse location, double userLat, double userLon) {
        final int R = 6371; // Bán kính trái đất (km)
        double latDistance = Math.toRadians(location.getLatitude() - userLat);
        double lonDistance = Math.toRadians(location.getLongitude() - userLon);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(location.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        location.setDistance(R * c); // Khoảng cách tính bằng km
    }

    public List<BusinessLocationResponse> searchLocations(SearchRequest request) {
        if (request == null || (request.getLocation() == null && request.getLatitude() == null && request.getLongitude() == null)) {
            throw new IllegalArgumentException("At least one of location, latitude, or longitude must be provided");
        }
        List<BusinessLocationResponse> locations = modelMapper.map(searchRepository.findAll(),
                new org.modelmapper.TypeToken<List<BusinessLocationResponse>>(){}.getType());

        // Lọc các location chưa bị xóa
//        locations = locations.stream()
//                .filter(location -> !location.getIsDelete())
//                .collect(Collectors.toList());

        // Nếu có tọa độ
        if (request.getLatitude() != null && request.getLongitude() != null &&
                request.getLatitude() != 0 && request.getLongitude() != 0) {
            return locations.stream()
                    .filter(location -> location.getLatitude() != null && location.getLongitude() != null)
                    .map(location -> {
                        calculateDistance(location, request.getLatitude(), request.getLongitude());
                        return location;
                    })
                    .sorted((l1, l2) -> Double.compare(l1.getDistance(), l2.getDistance()))
                    .collect(Collectors.toList());
        } else if (request.getLocation() != null && !request.getLocation().isEmpty()) {
            String searchTerm = request.getLocation().toLowerCase();
            return locations.stream()
                    .filter(location -> location.getAddress().toLowerCase().contains(searchTerm) ||
                            location.getName().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
        }
        return List.of();
    }


}
