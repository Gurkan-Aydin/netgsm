package com.example.netgsm.gsm;

import com.example.netgsm.model.Message;

import java.util.Date;
import java.util.List;

public interface NetGsmService {

    void sendSms(String title, List<Message> messages, Date startDate, Date stopDate);
}
