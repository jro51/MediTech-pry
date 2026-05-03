package com.medictech.purchase_service.exception;

public class PurchaseNotFoundException extends RuntimeException{
    public PurchaseNotFoundException(Long id) {
        super("Compra no encontrada con id: " + id);
    }
}
