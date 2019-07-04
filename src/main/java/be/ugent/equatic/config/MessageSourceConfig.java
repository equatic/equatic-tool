package be.ugent.equatic.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Configures a message source instance to reload when one of the properties files containing messages changes.
 * <p>
 * That reload mechanism is important for easy development.
 * <p>
 * TODO: Shouldn't be probably used on production as disabling caching incurs some performance loss.
 */
@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:Messages", "classpath:ValidationMessages");
        messageSource.setDefaultEncoding("utf-8");
        messageSource.setCacheSeconds(0);
        return messageSource;
    }
}