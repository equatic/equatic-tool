package be.ugent.equatic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "policy")
public class PolicyProperties {

    public static class SuperAdmin {

        private Boolean hasAllRoles;

        public Boolean getHasAllRoles() {
            return hasAllRoles;
        }

        public void setHasAllRoles(Boolean hasAllRoles) {
            this.hasAllRoles = hasAllRoles;
        }
    }

    private SuperAdmin superAdmin;

    public SuperAdmin getSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(SuperAdmin superAdmin) {
        this.superAdmin = superAdmin;
    }
}
