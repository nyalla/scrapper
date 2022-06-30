package com.tool.scrapper.schedulers;

import com.tool.scrapper.service.ScraperServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

public class RecurringNotifyService {

    @Autowired
    ScraperServiceImpl scraperService;

    @Scheduled(cron = "0 22 * * ?")
    public void cronJobSch() throws Exception {
        System.out.println("Trigger scheduler triggered");
        scraperService.extractPowerDataFromTsspdcl("","Narayanamma");
    }
}
