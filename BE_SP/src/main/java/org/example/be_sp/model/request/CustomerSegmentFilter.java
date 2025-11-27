package org.example.be_sp.model.request;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerSegmentFilter {
    private String segmentType;
    private String segmentKey;
    private String search;
    private Integer minOrdersCount;
    private Integer maxOrdersCount;
    private Long minTotalSpent;
    private Long maxTotalSpent;
    private LocalDate lastOrderFrom;
    private LocalDate lastOrderTo;
    private Integer page;
    private Integer pageSize;
    private Integer birthdayDays;
}

