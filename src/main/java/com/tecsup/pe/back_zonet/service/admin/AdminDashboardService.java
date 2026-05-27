package com.tecsup.pe.back_zonet.service.admin;

import com.tecsup.pe.back_zonet.dto.admin.DashboardDTO;
import com.tecsup.pe.back_zonet.repository.AdminStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardService {

    @Autowired
    private AdminStatsRepository statsRepo;

    public DashboardDTO getStats() {
        DashboardDTO dto = new DashboardDTO();

        dto.setTotalUsers(statsRepo.countTotalUsers());
        dto.setTotalPets(statsRepo.countTotalPets());
        dto.setActivePremium(statsRepo.countActivePremiumUsers());
        dto.setPendingTickets(statsRepo.countPendingTickets());

        Map<String, Long> statusMap = new HashMap<>();
        List<Object[]> results = statsRepo.countPetsByStatus();

        for (Object[] result : results) {
            String statusValue = (result[0] != null && !result[0].toString().trim().isEmpty())
                    ? (String) result[0]
                    : "CONECTADO";

            Long count = (Long) result[1];
            statusMap.put(statusValue, statusMap.getOrDefault(statusValue, 0L) + count);
        }

        dto.setDevicesStatus(statusMap);
        return dto;
    }
}