package be.ugent.equatic.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import be.ugent.equatic.exception.InstitutionNotSelectedBySuperAdminException;
import be.ugent.equatic.exception.UserMessageException;
import be.ugent.equatic.web.admin.superadmin.InstitutionSelectionController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(value = InstitutionNotSelectedBySuperAdminException.class)
    public ModelAndView institutionNotSelectedBySuperAdmin(HttpServletRequest request, Exception ex) {
        InstitutionNotSelectedBySuperAdminException exception = (InstitutionNotSelectedBySuperAdminException) ex;

        RedirectView redirectView = new RedirectView(
                request.getContextPath() + InstitutionSelectionController.VIEW_SELECT_INSTITUTION);

        ModelAndView modelAndView = new ModelAndView(redirectView, "nextAction",
                request.getContextPath() + request.getServletPath());
        modelAndView.addObject("virtual", exception.isVirtual());

        return modelAndView;
    }

    @ExceptionHandler(value = UserMessageException.class)
    public ModelAndView userMessageException(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        UserMessageException userMessageException = (UserMessageException) ex;

        response.setStatus(userMessageException.getHttpStatus().value());

        ModelAndView mv = new ModelAndView("error");
        mv.addObject("userMessage", userMessageException.getMessage(messageSource, request.getLocale()));

        return mv;
    }
}
