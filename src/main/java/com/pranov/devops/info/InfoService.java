package com.pranov.devops.info;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Deployment details shown in the site footer and returned by /api/info.
 * The version comes from pom.xml and the build number is injected by the pipeline,
 * so every redeployment is visible in the browser.
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
}
