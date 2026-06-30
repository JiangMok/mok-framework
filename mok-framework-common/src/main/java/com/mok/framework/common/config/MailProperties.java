package com.mok.framework.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    private String host;
    private int port;
    private boolean sslEnable;
    private boolean starttlsEnable;
    private String from;
    private String user;
    private String password;

    // Getter & Setter
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public boolean isSslEnable() { return sslEnable; }
    public void setSslEnable(boolean sslEnable) { this.sslEnable = sslEnable; }
    public boolean isStarttlsEnable() { return starttlsEnable; }
    public void setStarttlsEnable(boolean starttlsEnable) { this.starttlsEnable = starttlsEnable; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}