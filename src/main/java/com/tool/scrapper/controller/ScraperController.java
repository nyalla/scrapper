package com.tool.scrapper.controller;

import com.tool.scrapper.model.ResponseDTO;
import com.tool.scrapper.service.ScraperServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Set;

@RestController
public class ScraperController {

    @Autowired
    ScraperServiceImpl scraperService;

    @GetMapping(path = "/scrape")
    public Set<ResponseDTO> getVehicleByModel() {
        return  scraperService.extractTable("");
    }
}