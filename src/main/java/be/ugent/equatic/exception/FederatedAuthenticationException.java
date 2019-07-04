package be.ugent.equatic.exception;

/**
 * A wrapper for authentication exceptions thrown during federated sign in.
 */
public class FederatedAuthenticationException extends RuntimeException {

    public FederatedAuthenticationException(Throwable cause) {
        super(cause);
    }
}
