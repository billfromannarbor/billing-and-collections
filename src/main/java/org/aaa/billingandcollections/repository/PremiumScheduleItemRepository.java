package org.aaa.billingandcollections.repository;

import org.aaa.billingandcollections.model.ScheduleItemStatus;
import org.aaa.billingandcollections.model.entities.PremiumScheduleItem;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PremiumScheduleItemRepository {

    private final ConcurrentHashMap<UUID, PremiumScheduleItem> scheduleItems = new ConcurrentHashMap<>();

    public PremiumScheduleItem save(PremiumScheduleItem item) {
        scheduleItems.put(item.getScheduleItemId(), item);
        return item;
    }

    public List<PremiumScheduleItem> findByPolicyId(String policyId) {
        return scheduleItems.values()
                .stream()
                .filter(item -> item.getPolicyId().equals(policyId))
                .sorted((a, b) -> a.getDueDate().compareTo(b.getDueDate()))
                .toList();
    }

    public List<PremiumScheduleItem> findAll() {
        return scheduleItems.values().stream().toList();
    }

    public PremiumScheduleItem findById(UUID scheduleItemId) {
        return scheduleItems.get(scheduleItemId);
    }

    @PostConstruct
    private void seedData() {

        // Policy A schedule
        save(PremiumScheduleItem.builder()
                .scheduleItemId(UUID.randomUUID())
                .policyId("POLICY-1001")
                .dueDate(LocalDate.now().minusDays(10))
                .amount(new BigDecimal("120.00"))
                .status(ScheduleItemStatus.OVERDUE)
                .build());

        save(PremiumScheduleItem.builder()
                .scheduleItemId(UUID.randomUUID())
                .policyId("POLICY-1001")
                .dueDate(LocalDate.now().plusDays(20))
                .amount(new BigDecimal("120.00"))
                .status(ScheduleItemStatus.FUTURE)
                .build());

        // Policy B schedule
        save(PremiumScheduleItem.builder()
                .scheduleItemId(UUID.randomUUID())
                .policyId("POLICY-2001")
                .dueDate(LocalDate.now().minusDays(2))
                .amount(new BigDecimal("95.00"))
                .status(ScheduleItemStatus.OVERDUE)
                .build());

        save(PremiumScheduleItem.builder()
                .scheduleItemId(UUID.randomUUID())
                .policyId("POLICY-2001")
                .dueDate(LocalDate.now().plusDays(30))
                .amount(new BigDecimal("95.00"))
                .status(ScheduleItemStatus.FUTURE)
                .build());
    }
}