package com.tecsup.pe.back_zonet.repository;

import com.tecsup.pe.back_zonet.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
}
