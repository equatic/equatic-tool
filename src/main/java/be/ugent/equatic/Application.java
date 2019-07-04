package be.ugent.equatic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Validator;

@SpringBootApplication
@PropertySource(value = "file:${user.home}/conf/equatic.properties")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Gets a password encoder that will be used for the local eQuATIC accounts (stored in database).
     *
     * @return a password encoder, not null
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Validator localValidatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }
}
