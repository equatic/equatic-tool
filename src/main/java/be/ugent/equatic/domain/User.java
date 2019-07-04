package be.ugent.equatic.domain;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import be.ugent.equatic.web.util.RawPasswordAndConfirm;

@Entity
@Table(name = "users")
public class User implements RawPasswordAndConfirm, Serializable {

    @Id
    @SequenceGenerator(name = "users_seq_gen", sequenceName = "users_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq_gen")
    private Long id;

    @NotEmpty(message = "{User.username.NotEmpty}")
    @Size(max = 100)
    private String username;

    @NotEmpty(message = "{User.firstname.NotEmpty}")
    @Size(max = 100)
    private String firstname;

    @NotEmpty(message = "{User.lastname.NotEmpty}")
    @Size(max = 100)
    private String lastname;

    @ManyToOne
    @JoinColumn(name = "inst_id")
    @NotNull(message = "{User.institution.NotNull}")
    private Institution institution;

    @NotEmpty(message = "{User.email.NotEmpty}")
    @Size(max = 100)
    @Email(message = "{User.email.Email}")
    private String email;

    /**
     * Has email changed?trans
     */
    @Transient
    private boolean emailChanged = false;

    /**
     * Encoded password stored in the database.
     */
    @Size(max = 100)
    private String password;

    /**
     * Password as plaintext. Isn't stored in the database.
     */
    @Transient
    @Size(max = 100)
    private String rawPassword;

    /**
     * Field for confirmation password sent during registration (for validation only).
     */
    @Transient
    @Size(max = 100)
    private String confirmRawPassword;

    /**
     * Has the user confirmed the e-mail?
     */
    private boolean emailConfirmed = false;

    /**
     * Did the institutional administrator activate the user account? Can the user sign in?
     */
    private boolean activated = false;

    @Column(length = 36)
    private String token;

    /**
     * Only administrator roles are stored in this field!
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Role> adminRoles = new ArrayList<>();

    protected User() {
    }

    public User(String username, Institution institution) {
        this.username = username;
        this.institution = institution;
    }

    public User(String username, Institution institution, String firstname, String lastname, String email) {
        this.username = username;
        this.institution = institution;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }

    public List<Authority> getAuthorities() {
        return this.adminRoles.stream().map(Role::getRole).collect(Collectors.toList());
    }

    public boolean isSuperAdmin() {
        return getAuthorities().contains(Authority.ROLE_ADMIN_SUPER);
    }

    /**
     * Generates token for email confirmation or password resetting.
     *
     * @return String generated token
     */
    public String generateToken() {
        String token = UUID.randomUUID().toString();
        this.setToken(token);
        return token;
    }

    public String getDisplayName() {
        return getFirstname() + " " + getLastname();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (this.email != null && !this.email.equals(email)) {
            emailChanged = true;
        }
        this.email = email;
    }

    public boolean isEmailChanged() {
        return emailChanged;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public void setEmailConfirmed(Boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public List<Role> getAdminRoles() {
        return adminRoles;
    }

    public boolean isAdmin() {
        return !getAdminRoles().isEmpty();
    }

    public void setAdminRoles(List<Role> adminRoles) {
        this.adminRoles = adminRoles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", institution=" + institution +
                ", emailConfirmed=" + emailConfirmed +
                ", activated=" + activated +
                ", adminRoles=" + adminRoles +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (emailConfirmed != user.emailConfirmed) return false;
        if (activated != user.activated) return false;
        if (username != null ? !username.equals(user.username) : user.username != null) return false;
        if (firstname != null ? !firstname.equals(user.firstname) : user.firstname != null) return false;
        if (lastname != null ? !lastname.equals(user.lastname) : user.lastname != null) return false;
        if (institution != null ? !institution.equals(user.institution) : user.institution != null) return false;
        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        ArrayList<Role> adminRolesArrayList = new ArrayList<>(adminRoles);
        return !(adminRoles != null ? !adminRolesArrayList.equals(user.adminRoles) : user.adminRoles != null);
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (firstname != null ? firstname.hashCode() : 0);
        result = 31 * result + (lastname != null ? lastname.hashCode() : 0);
        result = 31 * result + (institution != null ? institution.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (emailConfirmed ? 1 : 0);
        result = 31 * result + (activated ? 1 : 0);
        result = 31 * result + (adminRoles != null ? adminRoles.hashCode() : 0);
        return result;
    }

    public void removeAuthority(Authority authority) {
        Role roleToRemove = new Role(authority, this);

        adminRoles.remove(roleToRemove);
    }

    public void addAuthority(Authority authority) {
        Role roleToAdd = new Role(authority, this);

        adminRoles.add(roleToAdd);
    }

    /**
     * CAUTION: This field is only set through registration form!
     *
     * @return password as plaintext
     */
    @Override
    public String getRawPassword() {
        return rawPassword;
    }

    public void setRawPassword(String rawPassword) {
        this.rawPassword = rawPassword;
    }

    @Override
    public String getConfirmRawPassword() {
        return confirmRawPassword;
    }

    public void setConfirmRawPassword(String confirmRawPassword) {
        this.confirmRawPassword = confirmRawPassword;
    }
}
