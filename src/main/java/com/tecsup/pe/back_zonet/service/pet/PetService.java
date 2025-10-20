package com.tecsup.pe.back_zonet.service.pet;

import com.tecsup.pe.back_zonet.entity.Pet;
import com.tecsup.pe.back_zonet.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    public Pet save(Pet pet) {
        return petRepository.save(pet);
    }
}
