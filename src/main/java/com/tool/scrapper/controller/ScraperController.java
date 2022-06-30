package com.tool.scrapper.controller;

import com.tool.scrapper.model.PowerDTO;
import com.tool.scrapper.model.ResponseDTO;
import com.tool.scrapper.service.ScraperServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Set;

@RestController
public class ScraperController {

    @Autowired
    ScraperServiceImpl scraperService;

    @GetMapping(path = "/scrape1")
    public Set<ResponseDTO> getVehicleByModel() {
        return  scraperService.extractTable("");
    }

    @GetMapping(path = "/scrape")
    public Set<PowerDTO> getVehicleByModel12(@RequestParam(name = "area",required = false) String outageArea) {
        return  scraperService.extractPowerData("", outageArea);
    }

    @GetMapping(path = "/power")
    public Set<PowerDTO> getVehicleByModel1(@RequestParam(name = "area",required = false) String outageArea) {
        return  scraperService.extractPowerDataFromTsspdcl("", outageArea);
    }
}