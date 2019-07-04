package be.ugent.equatic.exception;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import be.ugent.equatic.web.util.Message;
import be.ugent.equatic.web.util.MessageType;

import java.util.Arrays;
import java.util.Locale;

public class UserMessageException extends RuntimeException {

    private String messageCode;
    protected String[] params;
    MessageType messageType = MessageType.danger;
    HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

    UserMessageException(String messageCode, String... params) {
        this.messageCode = messageCode;
        this.params = params;
    }

    UserMessageException(String messageCode, Throwable cause) {
        super(cause);
        this.messageCode = messageCode;
    }

    public Message getMessage(MessageSource messageSource, Locale locale) {
        return new Message(getErrorDesc(messageSource, locale), messageType);
    }

    private String getErrorDesc(MessageSource messageSource, Locale locale) {
        return messageSource.getMessage(messageCode, params, locale);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof UserMessageException))
            return false;

        UserMessageException exception = (UserMessageException) obj;

        return this.messageCode.equals(exception.messageCode)
                && Arrays.equals(this.params, exception.params)
                && this.messageType == exception.messageType;
    }
}
