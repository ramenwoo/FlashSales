package com.example.flashsale.exception;

public class FlashSaleException extends RuntimeException {

    private final String code;

    public FlashSaleException(String message) {
        super(message);
        this.code = "FLASH_SALE_ERROR";
    }

    public FlashSaleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}