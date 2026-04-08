package com.drystorm.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class AvailableSlotsResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private Long serviceId;
    private Integer serviceDurationMinutes;
    private List<TimeSlot> availableSlots;

    @Data
    @Builder
    public static class TimeSlot {
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;

        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;

        private boolean available;
    }
}
