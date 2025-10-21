package com.tecsup.pe.back_zonet.exception;

// ❗ Se corrige para que extienda RuntimeException
public class PetNotFoundException extends RuntimeException {

    public PetNotFoundException() {
        super();
    }

    // Se agrega el constructor que acepta un mensaje (corrigiendo el error de LostPetService)
    public PetNotFoundException(String message) {
        super(message);
    }
}