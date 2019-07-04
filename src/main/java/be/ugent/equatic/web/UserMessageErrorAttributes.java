package be.ugent.equatic.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import be.ugent.equatic.web.util.Message;
import be.ugent.equatic.web.util.MessageType;

import java.util.Locale;
import java.util.Map;

@Component
public class UserMessageErrorAttributes extends DefaultErrorAttributes {

    @Autowired
    private MessageSource messageSource;

    @Override
    public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean includeStackTrace) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(requestAttributes, includeStackTrace);

        Locale locale = Locale.getDefault();
        if (requestAttributes instanceof ServletRequestAttributes) {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            locale = servletRequestAttributes.getRequest().getLocale();
        }

        Message message;
        switch ((Integer) errorAttributes.get("status")) {
            case 403:
                message = new Message(messageSource.getMessage("equatic.notAuthorizedException", null, locale),
                        MessageType.danger);
                break;
            case 404:
                message = new Message(messageSource.getMessage("equatic.pageNotFoundException", null, locale),
                        MessageType.warning);
                break;
            default:
                message = new Message(messageSource.getMessage("equatic.unhandledException", null, locale),
                        MessageType.danger);
        }
        errorAttributes.put("userMessage", message);

        return errorAttributes;
    }
}
