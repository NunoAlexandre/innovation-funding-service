package org.innovateuk.ifs.application.service;

import org.innovateuk.ifs.alert.resource.AlertType;
import org.innovateuk.ifs.alert.resource.AlertResource;
import org.innovateuk.ifs.alert.service.AlertRestService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class contains methods to retrieve and store {@link AlertResource} related data,
 * through the RestService {@link AlertRestService}.
 */
@Service
public class AlertServiceImpl implements AlertService {

    @Autowired
    private AlertRestService alertRestService;

    @Override
    public List<AlertResource> findAllVisible() {
        return alertRestService.findAllVisible().getSuccessObjectOrThrowException();
    }

    @Override
    public List<AlertResource> findAllVisibleByType(final AlertType alertType) {
        return alertRestService.findAllVisibleByType(alertType).getSuccessObjectOrThrowException();
    }

    @Override
    public AlertResource getById(final Long id) {
        return alertRestService.getAlertById(id).getSuccessObjectOrThrowException();
    }

    @Override
    public ServiceResult<AlertResource> create(final AlertResource alertResource) {
        return alertRestService.create(alertResource).toServiceResult();
    }

    @Override
    public ServiceResult<Void> delete(final Long id) {
        return alertRestService.delete(id).toServiceResult();
    }

    @Override
    public ServiceResult<Void> deleteAllByType(final AlertType type) {
        return alertRestService.deleteAllByType(type).toServiceResult();
    }
}
