package com.tool.scrapper.schedulers;

import com.tool.scrapper.service.ScraperServiceImpl;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import javax.net.ssl.*;
import java.security.cert.CertificateException;

public class RecurringNotifyService {

    @Autowired
    ScraperServiceImpl scraperService;

    @Scheduled(cron = "0 12 * * ?")
    public void cronJobSch() throws Exception {
        System.out.println("Trigger scheduler triggered");
         OkHttpClient client = getUnsafeOkHttpClient();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://api.telegram.org/bot5576678061:AAH_bsRWn_EFqvUi8ZM1GrM-EJ0QtaNdIuE/sendMessage?chat_id=-1001540845770&text=TriggeredAuto" )
                .method("GET", null)
                .build();
        Response response = client.newCall(request).execute();

        scraperService.extractPowerDataFromTsspdcl("","NARAYANAMMA");
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
