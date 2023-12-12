package de.hasait.teleport.service.ssh;

import java.util.List;

public class SshHostConnectConfig {

    public static final String DEFAULT_SSH_USER = "root";
    public static final List<String> DEFAULT_SSH_ARGS = List.of("-A", "-o", "BatchMode=yes");

    private String user;
    private String host;
    private List<String> args;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

}
