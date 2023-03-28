package com.example.netgsm.model;

import lombok.Getter;

@Getter
public enum NetGsmStatus {
    WAITING(0),
    SENT(1),
    EXPIRED(2),
    WRONG_NUMBER(3),
    COULDT_SEND_OPERATOR(4),
    NOT_ACCEPTED(11),
    SEND_ERROR(12),
    DUPLICATE(13),
    ALL(100),
    FAILED(103);

    private final int code;

    NetGsmStatus(int code) {
        this.code = code;
    }




}
