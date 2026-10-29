package com.pranov.devops.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Builds the information shown on the home page and returned by /api/info.
 * Version and build values are injected by the pipeline as environment variables,
 * so a redeployment is visible directly in the browser.
 */
@Service
public class InfoService {

    private final String appName;
    private final String appVersion;
    private final String buildNumber;
    private final String message;
    private final Instant startedAt = Instant.now();

    public InfoService(@Value("${app.name}") String appName,
                       @Value("${app.version}") String appVersion,
                       @Value("${app.build}") String buildNumber,
                       @Value("${app.message}") String message) {
        this.appName = appName;
        this.appVersion = appVersion;
        this.buildNumber = buildNumber;
        this.message = message;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public String getMessage() {
        return message;
    }

    /** Pod name when running in Kubernetes, machine name otherwise. */
    public String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    public Map<String, String> getInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("application", appName);
        info.put("version", appVersion);
        info.put("build", buildNumber);
        info.put("message", message);
        info.put("hostname", getHostname());
        info.put("startedAt", startedAt.toString());
        return info;
    }

    /** Simple business logic so the unit tests cover more than wiring. */
    public static int add(int a, int b) {
        return a + b;
    }
}
