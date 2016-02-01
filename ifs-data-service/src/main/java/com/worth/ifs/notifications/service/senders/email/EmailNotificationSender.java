package com.worth.ifs.notifications.service.senders.email;

import com.worth.ifs.email.resource.EmailAddress;
import com.worth.ifs.email.service.EmailService;
import com.worth.ifs.notifications.resource.Notification;
import com.worth.ifs.notifications.resource.NotificationMedium;
import com.worth.ifs.notifications.resource.NotificationTarget;
import com.worth.ifs.notifications.service.NotificationSender;
import com.worth.ifs.notifications.service.NotificationTemplateRenderer;
import com.worth.ifs.transactional.Error;
import com.worth.ifs.transactional.ErrorTemplate;
import com.worth.ifs.transactional.ServiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.worth.ifs.notifications.resource.NotificationMedium.EMAIL;
import static com.worth.ifs.notifications.service.senders.email.EmailAddressResolver.fromNotificationSource;
import static com.worth.ifs.notifications.service.senders.email.EmailAddressResolver.fromNotificationTarget;
import static com.worth.ifs.notifications.service.senders.email.EmailNotificationSender.ServiceFailures.EMAILS_NOT_SENT;
import static com.worth.ifs.transactional.ServiceResult.*;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static java.io.File.separator;
import static java.util.Arrays.asList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * A Notification Sender that can, given a Notification, construct an email from it and use the Email Service to send
 * the email to the given recipients
 */
@Component
public class EmailNotificationSender implements NotificationSender {

    static final String EMAIL_NOTIFICATION_TEMPLATES_PATH = "notifications" + separator + "email" + separator;

    public enum ServiceFailures implements ErrorTemplate {
        EMAILS_NOT_SENT("The emails could not be sent", INTERNAL_SERVER_ERROR)
        ;

        private String errorMessage;
        private HttpStatus category;

        ServiceFailures(String errorMessage, HttpStatus category) {
            this.errorMessage = errorMessage;
            this.category = category;
        }

        @Override
        public String getErrorKey() {
            return name();
        }

        @Override
        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public HttpStatus getCategory() {
            return category;
        }
    }

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationTemplateRenderer renderer;

    @Override
    public NotificationMedium getNotificationMedium() {
        return EMAIL;
    }

    @Override
    public ServiceResult<Notification> sendNotification(Notification notification) {

        return handlingErrors(new Error(EMAILS_NOT_SENT), () -> {

            EmailAddress from = fromNotificationSource(notification.getFrom());

            List<ServiceResult<List<EmailAddress>>> results = simpleMap(notification.getTo(), recipient ->
                getSubject(notification, recipient).map(subject ->
                getPlainTextBody(notification, recipient).map(plainTextBody ->
                getHtmlBody(notification, recipient).map(htmlBody ->
                    emailService.sendEmail(from, asList(fromNotificationTarget(recipient)), subject, plainTextBody, htmlBody)
                )))
            );

            return anyFailures(results, serviceFailure(new Error(EMAILS_NOT_SENT)), serviceSuccess(notification));
        });
    }

    private ServiceResult<String> getSubject(Notification notification, NotificationTarget recipient) {
        return renderer.renderTemplate(notification.getFrom(), recipient, getTemplatePath(notification, "subject") + ".txt", notification.getArguments());
    }

    private ServiceResult<String> getPlainTextBody(Notification notification, NotificationTarget recipient) {
        return renderer.renderTemplate(notification.getFrom(), recipient, getTemplatePath(notification, "text_plain") + ".txt", notification.getArguments());
    }

    private ServiceResult<String> getHtmlBody(Notification notification, NotificationTarget recipient) {
        return renderer.renderTemplate(notification.getFrom(), recipient, getTemplatePath(notification, "text_html") + ".html", notification.getArguments());
    }

    private String getTemplatePath(Notification notification, String suffix) {
        return EMAIL_NOTIFICATION_TEMPLATES_PATH + notification.getMessageKey().name().toLowerCase() + "_" + suffix;
    }
}
