package com.law.blueinnofora;

/**
 * Created by gd2 on 2015-07-02.
 */
public class BleNotAvailableException extends RuntimeException {
    private static final long serialVersionUID = 2242747823097637729L;

    public BleNotAvailableException(String message) {
        super(message);
    }
}
