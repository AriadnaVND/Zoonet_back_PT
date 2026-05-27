package com.tecsup.pe.back_zonet.controller.admin;

import com.tecsup.pe.back_zonet.dto.admin.DashboardDTO;
import com.tecsup.pe.back_zonet.service.admin.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardDTO getSummary() {
        return dashboardService.getStats();
    }
}