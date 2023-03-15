package com.example.netgsm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.beans.SimpleBeanInfo;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class NetGsmServiceImp implements NetGsmService {

    @Value("${netgsm.username}")
    private String username;
    @Value("${netgsm.password}")
    private String password;

    //https://www.netgsm.com.tr/dokuman/?language=JAVA#http-post-sms-g%C3%B6nderme
     public void sendSms(String title, List<Message> messages, Date startDate, Date stopDate) {
        try {
            URL u = new URL("https://api.netgsm.com.tr/sms/send/xml");

            URLConnection uc = u.openConnection();
            HttpURLConnection connection = (HttpURLConnection) uc;
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            OutputStream out = connection.getOutputStream();
            OutputStreamWriter wout = new OutputStreamWriter(out, "UTF-8");
            wout.write("<?xml version='1.0' encoding='iso-8859-9'?> "+
                    "<mainbody>"+
                    "<header>"+
                    "<company dil='TR'>Netgsm</company>"+
                    "<usercode>" + username + "</usercode>"+
                    "<password>" + password + "</password>"+
                    getFormattedDate(startDate, stopDate)+
                    "<type>n:n</type>"+
                    "<msgheader>" + title + "</msgheader>"+
                    "</header>"+
                    "<body>"+
                    getFormattedMessages(messages)+
                    "</body>"+
                    "</mainbody>");
            wout.flush();
            out.close();
            InputStream in = connection.getInputStream();
            int c;
            StringBuilder result = new StringBuilder();
            while ((c = in.read()) != -1){
                result.append((char) c);
            }
            in.close();
            out.close();
            connection.disconnect();

            System.out.println("rapor durum = " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFormattedDate(Date startDate, Date stopDate) {
         String response = "";
         if (startDate != null && startDate.after(new Date())) {
            response += "<startdate>" + getDate(startDate) + "</startdate> ";
         }
        if (stopDate != null && stopDate.after(new Date())) {
            response += "<stopdate>" + getDate(stopDate) + "</stopdate> ";
        }
        return response;
    }

    private String getDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmm");
        return formatter.format(date);
    }

    private String getFormattedMessages(List<Message> messages) {
        StringBuilder response = new StringBuilder();
        for (Message message: messages) {
            response.append("<mp><msg><![CDATA[").append(message.getMessageText()).append("]]></msg><no>").append(message.getGsmNumber()).append("</no></mp>");
        }
        return response.toString();
    }
}
