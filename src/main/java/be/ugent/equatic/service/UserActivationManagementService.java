package be.ugent.equatic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.exception.AccessDeniedException;
import be.ugent.equatic.web.util.Message;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.util.Locale;

@Service
public class UserActivationManagementService {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MessageSource messageSource;

    public Message activateUser(User target, User admin, HttpServletRequest request, Locale locale)
            throws MalformedURLException {
        if (!target.isEmailConfirmed()) {
            throw new AccessDeniedException("E-mail has to be confirmed");
        }
        if (target.isActivated()) {
            throw new AccessDeniedException("User is already activated");
        }

        target.setActivated(true);
        userService.save(target);

        if (target.getPassword() == null && !target.getInstitution().isWithFederatedIdP()) {
            target.generateToken();
            userService.save(target);

            notificationService.sendActivationAndPasswordResetNotification(target, admin, locale, request);
        } else {
            notificationService.sendActivationNotification(target, admin, locale, request);
        }

        String userActivatedMessage = messageSource.getMessage("InstitutionalAdminController.activate.confirmation",
                new String[]{target.getUsername()}, locale);
        return Message.success(userActivatedMessage);
    }

    public Message deactivateUser(User target, User admin, Locale locale) {
        if (!target.isEmailConfirmed()) {
            throw new AccessDeniedException("E-mail has to be confirmed");
        }
        if (!target.isActivated()) {
            throw new AccessDeniedException("User is already deactivated");
        }

        target.setActivated(false);
        userService.save(target);

        notificationService.sendDeactivationNotification(target, admin, locale);

        String userDeactivatedMessage = messageSource.getMessage("InstitutionalAdminController.deactivate.confirmation",
                new String[]{target.getUsername()}, locale);
        return Message.success(userDeactivatedMessage);
    }
}
