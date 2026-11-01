package com.pranov.devops.info;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

    private final InfoService infoService;

    public InfoController(InfoService infoService) {
        this.infoService = infoService;
    }

    /** Also used by the pipeline smoke test to confirm which build is serving traffic. */
    @GetMapping("/api/info")
    public Map<String, String> info() {
        return infoService.getInfo();
    }
}
