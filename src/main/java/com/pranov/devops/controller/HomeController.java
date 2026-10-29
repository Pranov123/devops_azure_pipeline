package com.pranov.devops.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import com.pranov.devops.service.InfoService;

@RestController
public class HomeController {

    private final InfoService infoService;

    public HomeController(InfoService infoService) {
        this.infoService = infoService;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>%s</title>
                  <style>
                    body { font-family: Arial, sans-serif; background: #f2f5f9; margin: 0; padding: 40px 16px; color: #1d2733; }
                    .card { max-width: 640px; margin: 0 auto; background: #fff; border-radius: 10px; padding: 32px; box-shadow: 0 2px 10px rgba(0,0,0,.08); }
                    h1 { margin-top: 0; color: #0b63c5; }
                    .msg { font-size: 1.2em; margin-bottom: 24px; }
                    table { width: 100%%; border-collapse: collapse; }
                    td { padding: 8px 0; border-bottom: 1px solid #e6eaf0; }
                    td:first-child { color: #5b6b7d; width: 40%%; }
                    footer { margin-top: 24px; font-size: .85em; color: #5b6b7d; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>%s</h1>
                    <p class="msg">%s</p>
                    <table>
                      <tr><td>Version</td><td>%s</td></tr>
                      <tr><td>Jenkins build</td><td>%s</td></tr>
                      <tr><td>Served by pod</td><td>%s</td></tr>
                    </table>
                    <footer>Git &rarr; Jenkins &rarr; Maven &rarr; Docker &rarr; Azure Container Registry &rarr; Ansible &rarr; Kubernetes</footer>
                  </div>
                </body>
                </html>
                """.formatted(
                HtmlUtils.htmlEscape(infoService.getAppName()),
                HtmlUtils.htmlEscape(infoService.getAppName()),
                HtmlUtils.htmlEscape(infoService.getMessage()),
                HtmlUtils.htmlEscape(infoService.getAppVersion()),
                HtmlUtils.htmlEscape(infoService.getBuildNumber()),
                HtmlUtils.htmlEscape(infoService.getHostname()));
    }

    @GetMapping("/api/info")
    public Map<String, String> info() {
        return infoService.getInfo();
    }

    @GetMapping("/api/add")
    public Map<String, Integer> add(@RequestParam int a, @RequestParam int b) {
        return Map.of("result", InfoService.add(a, b));
    }
}
