package be.ugent.equatic.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Configures e-mail sender that is used for sending e-mails with notifications for users and administrators.
 * <p>
 * Uses {@link MailProperties} to configure e-mail sender.
 * <p>
 * TODO: Doesn't currently support any specific configuration like transport protocols.
 */
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {

    @Autowired
    MailProperties mailProperties;

    @Bean
    public JavaMailSenderImpl emailSender() {
        JavaMailSenderImpl emailSender = new JavaMailSenderImpl();
        emailSender.setHost(mailProperties.getHost());
        emailSender.setPort(mailProperties.getPort());

        Properties mailProps = new Properties();
        mailProps.setProperty("mail.transport.protocol", "smtp");
        mailProps.setProperty("mail.smtp.auth", "false");
        mailProps.setProperty("mail.smtp.starttls.enable", "true");
        mailProps.setProperty("mail.debug", "false");
        emailSender.setJavaMailProperties(mailProps);

        return emailSender;
    }
}
