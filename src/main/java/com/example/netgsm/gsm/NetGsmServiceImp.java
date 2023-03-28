package com.example.netgsm.gsm;

import com.example.netgsm.model.Message;
import com.example.netgsm.model.NetGsmStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NetGsmServiceImp implements NetGsmService {

    @Value("${netgsm.username}")
    private String username;
    @Value("${netgsm.password}")
    private String password;

    private String sendXML(String xml) {
        try {
            URL u = new URL("https://api.netgsm.com.tr/sms/send/xml");

            URLConnection uc = u.openConnection();
            HttpURLConnection connection = (HttpURLConnection) uc;
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            OutputStream out = connection.getOutputStream();
            OutputStreamWriter wout = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            wout.write(xml);
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

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    //https://www.netgsm.com.tr/dokuman/?language=JAVA#http-post-sms-g%C3%B6nderme
     public void sendSms(String title, List<Message> messages, Date startDate, Date stopDate) {
        String result = sendXML("<?xml version='1.0' encoding='UTF-8'?> "+
                "<mainbody>"+
                "<header>"+
                "<company dil='TR'>Netgsm</company>"+
                "<usercode>" + username + "</usercode>"+
                "<password>" + password + "</password>"+
                getXMLDate(startDate, stopDate)+
                "<type>n:n</type>"+
                "<msgheader>" + title + "</msgheader>"+
                "</header>"+
                "<body>"+
                getXMLMessages(messages)+
                "</body>"+
                "</mainbody>");

         System.out.println(result);
    }

    public void updateSmsDate(Long bulkId, Date start, Date end){
        String result = sendXML("<?xml version='1.0' encoding='UTF-8'?> "+
                "<mainbody>"+
                "<header>"+
                "<usercode>" + username + "</usercode>"+
                "<password>" + password + "</password>"+
                getXMLDate(start, end)+
                "<gorevid>" + bulkId + "</gorevid> "+
                "<type>1</type>"+
                "</header>"+
                "</mainbody>");

        System.out.println(result.equals("00"));
    }

    public void cancelSms(Long bulkId, Date start, Date end){
        String result = sendXML("<?xml version='1.0' encoding='UTF-8'?> "+
                "<mainbody>"+
                "<header>"+
                "<usercode>" + username + "</usercode>"+
                "<password>" + password + "</password>"+
                getXMLDate(start, end)+
                "<gorevid>" + bulkId + "</gorevid> "+
                "<type>0</type>"+
                "</header>"+
                "</mainbody>");

        System.out.println(result.equals("00"));
    }


    public void getMessage(List<Long> bulkIds, NetGsmStatus status, List<String> telNoList, String title, Date start, Date end) {
        StringBuilder url = new StringBuilder("https://api.netgsm.com.tr/sms/report/?usercode=" + username + "&password=" + password);
        if (!bulkIds.isEmpty()) {
            url.append("&type=").append((bulkIds.size() > 1) ? 1 : 0);
            url.append("&bulkid=");
            url.append(bulkIds.stream().map(String::valueOf).collect(Collectors.joining(", ")));
        }
        if (status != null) {
            url.append("&status=").append(status.getCode());
            url.append("&version=").append(2);
        }
        if (!telNoList.isEmpty()) {
            url.append("&telno=").append(String.join(",", telNoList));
        }
        if (title.isEmpty()) {
            url.append("&mbaslik=").append(title);
        }
        if (bulkIds.isEmpty() && (start != null ||end != null)) {
            url.append("&type=").append(3);
            if (start != null) url.append("&bastar=").append(getFormattedDate(start));
            if (end != null) url.append("&bittar=").append(getFormattedDate(end));
        }

        try {
        URL obj = new URL(url.toString());
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        /**
         * kodlar için https://www.netgsm.com.tr/dokuman/?language=JAVA#http-get-rapor
         */
        List<String> keys = Arrays.asList("bulkId", "telno", "durum", "operator", "msg boyu", "tarih", "saat", "hata kodu");
        if (bulkIds.isEmpty()) keys.remove(0);
        String[] responseArray = response.toString().split(" ");
        IntStream.range(0, responseArray.length).forEach(i -> {
            System.out.println(keys.get(i % keys.size()) + " = " + responseArray[i]);
        });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getXMLDate(Date startDate, Date stopDate) {
         String response = "";
         if (startDate != null && startDate.after(new Date())) {
            response += "<startdate>" + getFormattedDate(startDate) + "</startdate> ";
         }
        if (stopDate != null && stopDate.after(new Date())) {
            response += "<stopdate>" + getFormattedDate(stopDate) + "</stopdate> ";
        }
        return response;
    }

    private String getFormattedDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmm");
        return formatter.format(date);
    }

    private String getXMLMessages(List<Message> messages) {
        StringBuilder response = new StringBuilder();
        for (Message message: messages) {
            response.append("<mp><msg><![CDATA[").append(message.getMessageText()).append("]]></msg><no>").append(message.getGsmNumber()).append("</no></mp>");
        }
        return response.toString();
    }
}
