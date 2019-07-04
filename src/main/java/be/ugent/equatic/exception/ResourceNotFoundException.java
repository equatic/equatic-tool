package be.ugent.equatic.exception;

import org.springframework.http.HttpStatus;
import be.ugent.equatic.web.util.MessageType;

public class ResourceNotFoundException extends UserMessageException {

    ResourceNotFoundException(String messageCode, String... params) {
        super(messageCode, params);

        this.messageType = MessageType.warning;
        this.httpStatus = HttpStatus.NOT_FOUND;
    }
}
