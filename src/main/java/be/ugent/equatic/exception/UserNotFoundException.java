package be.ugent.equatic.exception;

import be.ugent.equatic.domain.Institution;

public class UserNotFoundException extends ResourceNotFoundException {

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private Institution institution;

    public UserNotFoundException(String messageCode, String... params) {
        super(messageCode, params);
    }

    /**
     * User with this ID not found.
     *
     * @param id the ID to search for
     */
    public UserNotFoundException(long id) {
        super("equatic.UserNotFoundException.byId", String.valueOf(id));
    }

    /**
     * User with this username not found in this institution.
     *
     * @param username    the username to search for
     * @param institution the institution to search in
     */
    public UserNotFoundException(String username, Institution institution) {
        super("equatic.UserNotFoundException.byUsernameAndInstitution", username, institution.getDisplayName());

        this.username = username;
        this.institution = institution;
    }

    public static UserNotFoundException byEmailAndInstitution(String email, Institution institution) {
        return new UserNotFoundException("equatic.UserNotFoundException.byEmailAndInstitution", email,
                institution.getDisplayName());
    }

    /**
     * User with this token not found.
     *
     * @param token the user's token to search for
     */
    public UserNotFoundException(String token) {
        super("equatic.UserNotFoundException.byToken", token);
    }

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

    public Institution getInstitution() {
        return institution;
    }
}
