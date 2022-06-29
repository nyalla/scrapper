package com.tool.scrapper.service;


import com.tool.scrapper.model.ResponseDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

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
}