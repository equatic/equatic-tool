package be.ugent.equatic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Stores configuration for SAML keystore and Service Provider used by eQuATIC tool for federation sign in.
 */
@Configuration
@ConfigurationProperties(prefix = "saml")
public class SamlProperties {

    public static class Keystore {

        private String file;
        private String key;
        private String password;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Sp {

        private String entityBaseURL;
        private String entityId;
        private String id;

        public String getEntityBaseURL() {
            return entityBaseURL;
        }

        public void setEntityBaseURL(String entityBaseURL) {
            this.entityBaseURL = entityBaseURL;
        }

        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class Attribute {

        private String firstName;
        private String lastName;
        private String email;
        private String username;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    private Keystore keystore;
    private Sp sp;
    private Attribute attribute;

    public Keystore getKeystore() {
        return keystore;
    }

    public void setKeystore(Keystore keystore) {
        this.keystore = keystore;
    }

    public Sp getSp() {
        return sp;
    }

    public void setSp(Sp sp) {
        this.sp = sp;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }
}
