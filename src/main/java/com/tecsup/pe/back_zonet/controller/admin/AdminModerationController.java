package com.tecsup.pe.back_zonet.controller.admin;

import com.tecsup.pe.back_zonet.entity.AdminModerationLog;
import com.tecsup.pe.back_zonet.repository.ModerationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/moderacion")
public class AdminModerationController {

    @Autowired
    private ModerationRepository moderationRepository;

    @GetMapping("/pendientes")
    public List<AdminModerationLog> obtenerPendientes() {
        return moderationRepository.findByStatus("PENDING");
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("procesadas", moderationRepository.count());
        stats.put("revisionManual", moderationRepository.countByStatus("MANUAL_REVIEW"));
        return ResponseEntity.ok(stats);
    }
}