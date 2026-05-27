package com.tecsup.pe.back_zonet.controller.admin;

import com.tecsup.pe.back_zonet.entity.LostPet;
import com.tecsup.pe.back_zonet.repository.LostPetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    @Autowired
    private LostPetRepository lostPetRepository;

    @GetMapping("/lost-pets")
    public List<LostPet> getLostPets() {
        return lostPetRepository.findAll();
    }
}