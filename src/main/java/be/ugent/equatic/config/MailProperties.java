package be.ugent.equatic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Stores SMTP configuration and reply address and subject prefix used for notifications sent from the eQuATIC tool.
 */
@Configuration
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    private String host;
    private Integer port;

    /**
     * The address used in "from:" for notifications sent from the eQuATIC tool.
     */
    private String replyAddress;

    /**
     * Prefix to put before the subject.
     * Can be used to differentiate between environments: PRD, QAS and TST.
     */
    private String subjectPrefix;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getReplyAddress() {
        return replyAddress;
    }

    public void setReplyAddress(String replyAddress) {
        this.replyAddress = replyAddress;
    }

    public String getSubjectPrefix() {
        return subjectPrefix;
    }

    public void setSubjectPrefix(String subjectPrefix) {
        this.subjectPrefix = subjectPrefix;
    }
}
