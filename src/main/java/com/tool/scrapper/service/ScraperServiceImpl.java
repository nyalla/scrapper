package com.tool.scrapper.service;


import com.tool.scrapper.model.PowerDTO;
import com.tool.scrapper.model.ResponseDTO;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScraperServiceImpl {
    public Set<ResponseDTO> extractTable(String url) {
        Set<ResponseDTO> responseDTOS = new HashSet<>();
        try {

            //loading the HTML to a Document Object
            Document document = Jsoup.connect("https://www.bankexamstoday.com/2016/10/indias-rankings-in-different-indexes.html").sslSocketFactory(socketFactory()).get();

            //Selecting the element which contains the ad list
            Element element = document.getElementsByClass("post-body entry-content").first();
            //getting all the <a> tag elements inside the list-       -3NxGO class
            Elements trElements = element.getElementsByTag("tr");

            for (Element trElement : trElements) {
                ResponseDTO res = new ResponseDTO();
                Elements tdElements = trElement.getElementsByTag("td");
                Object[] blockData = tdElements.toArray();

                if (blockData.length == 0)
                    continue;

                res.setIndex(tdElements.get(0).childNodes().get(0).toString());
                res.setIndexReleasedBy(tdElements.get(1).childNodes().get(0).toString());
                res.setIndiasRank(tdElements.get(2).childNodes().get(0).toString());
                res.setFirstRank(tdElements.get(3).childNodes().get(0).toString());
                responseDTOS.add(res);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return responseDTOS;
    }

    private SSLSocketFactory socketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to create a SSL socket factory", e);
        }
    }


    public Set<PowerDTO> extractPowerData(String url,String outageArea) {
        Set<PowerDTO> responseDTOS = new HashSet<>();
        try {

            File input = new File("C:/gear/personal/5to10/power.html");
            Document document = Jsoup.parse(input, "UTF-8", "");
            //Selecting the element which contains the ad list
            Element element = document.getElementsByTag("table").first();
            //getting all the <a> tag elements inside the list-       -3NxGO class
            Elements trElements = element.getElementsByTag("tr");
            int count = 0;
            for (Element trElement : trElements) {
                if (trElement.hasAttr("bgcolor")) {
                    count++;
                    Elements tdElements = trElement.getElementsByTag("td");
                    PowerDTO power = new PowerDTO();
                    power.setDate(((Element) tdElements.get(1).childNodes().get(0)).text());
                    power.setFeederName(((Element) tdElements.get(9).childNodes().get(0)).text());
                    power.setFrom(((Element) tdElements.get(11).childNodes().get(0)).text());
                    power.setTo(((Element) tdElements.get(12).childNodes().get(0)).text());
                    power.setType(((Element) tdElements.get(13).childNodes().get(0)).text());
                    responseDTOS.add(power);
                }
            }
            System.out.println(count);
            postAlertToTelegram(responseDTOS, outageArea);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return responseDTOS;
    }

    public Set<PowerDTO> extractPowerDataFromTsspdcl(String url, String outageArea) {
        Set<PowerDTO> responseDTOS = new HashSet<>();
        try {

            //loading the HTML to a Document Object
            Document document = Jsoup.connect("http://210.212.220.126:8080/TSSPDCL/Information/ScheduledOutageInformation.jsp").sslSocketFactory(socketFactory()).get();
            //Selecting the element which contains the ad list
            Element element = document.getElementsByTag("table").first();
            //getting all the <a> tag elements inside the list-       -3NxGO class
            Elements trElements = element.getElementsByTag("tr");
            int count = 0;
            for (Element trElement : trElements) {
                if (trElement.hasAttr("bgcolor")) {
                    count++;
                    Elements tdElements = trElement.getElementsByTag("td");
                    PowerDTO power = new PowerDTO();
                    power.setDate(((Element) tdElements.get(1).childNodes().get(0)).text());
                    power.setFeederName(((Element) tdElements.get(9).childNodes().get(0)).text());
                    power.setFrom(((Element) tdElements.get(11).childNodes().get(0)).text());
                    power.setTo(((Element) tdElements.get(12).childNodes().get(0)).text());
                    power.setType(((Element) tdElements.get(13).childNodes().get(0)).text());
                    responseDTOS.add(power);
                }
            }
            postAlertToTelegram(responseDTOS, outageArea);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return responseDTOS;
    }

    @Async
    private void postAlertToTelegram(Set<PowerDTO> responseDTOS, String outageArea) throws IOException {
        //https://api.telegram.org/bot5576678061:AAH_bsRWn_EFqvUi8ZM1GrM-EJ0QtaNdIuE/sendMessage?chat_id=-1001540845770&text=by_bot

        String alertData = responseDTOS.stream().filter(i -> i.getFeederName().contains(outageArea == null && outageArea.isEmpty() ? "TOURIST" : outageArea.toUpperCase())).map(i -> i.getFeederName() + ": Power outage from " + i.getFrom() + " to " + i.getTo() + " due to " + i.getType()).collect(Collectors.joining(","));
        if (alertData == null || alertData.isEmpty())
            alertData = "No outage in given "+outageArea+" Area";
        OkHttpClient client = getUnsafeOkHttpClient();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://api.telegram.org/bot5576678061:AAH_bsRWn_EFqvUi8ZM1GrM-EJ0QtaNdIuE/sendMessage?chat_id=-1001540845770&text=" + alertData)
                .method("GET", null)
                .build();
        Response response = client.newCall(request).execute();

    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}