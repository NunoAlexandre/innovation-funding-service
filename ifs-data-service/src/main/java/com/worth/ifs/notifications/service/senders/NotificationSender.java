package com.worth.ifs.notifications.service.senders;

import com.worth.ifs.notifications.resource.Notification;
import com.worth.ifs.notifications.resource.NotificationMedium;
import com.worth.ifs.commons.service.ServiceResult;

/**
 * Represents a Component that knows how to send a NotificationResponse out as a message via a particular NotificationMedium
 */
public interface NotificationSender {

    NotificationMedium getNotificationMedium();

    ServiceResult<Notification> sendNotification(Notification notification);
}
