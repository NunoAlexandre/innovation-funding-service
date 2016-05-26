package com.worth.ifs.alert.builder;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.BuilderAmendFunctions;
import com.worth.ifs.alert.resource.AlertType;
import com.worth.ifs.alert.resource.AlertResource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;

import static com.worth.ifs.BuilderAmendFunctions.setField;
import static com.worth.ifs.BuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

/**
 * Builder for {@link AlertResource}
 */
public class AlertResourceBuilder extends BaseBuilder<AlertResource, AlertResourceBuilder> {

    private AlertResourceBuilder(final List<BiConsumer<Integer, AlertResource>> newActions) {
        super(newActions);
    }

    public static AlertResourceBuilder newAlertResource() {
        return new AlertResourceBuilder(emptyList())
                .with(uniqueIds())
                .withMessage("Sample message")
                .withType(AlertType.MAINTENANCE)
                .withValidFromDate(LocalDateTime.parse("2016-05-06T21:00:00.00"))
                .withValidToDate(LocalDateTime.parse("2016-05-06T21:05:00.00"));
    }

    @Override
    protected AlertResourceBuilder createNewBuilderWithActions(final List<BiConsumer<Integer, AlertResource>> actions) {
        return new AlertResourceBuilder(actions);
    }

    @Override
    protected AlertResource createInitial() {
        return new AlertResource();
    }

    public AlertResourceBuilder withId(final Long... ids) {
        return withArray(BuilderAmendFunctions::setId, ids);
    }

    public AlertResourceBuilder withMessage(final String... messages) {
        return withArray((message, alertResource) -> setField("message", message, alertResource), messages);
    }

    public AlertResourceBuilder withType(final Enum<?>... types) {
        return withArray((type, alertResource) -> setField("type", type, alertResource), types);
    }

    public AlertResourceBuilder withValidFromDate(final LocalDateTime... validFromDates) {
        return withArray((validFromDate, alertResource) -> setField("validFromDate", validFromDate, alertResource), validFromDates);
    }

    public AlertResourceBuilder withValidToDate(final LocalDateTime... validToDates) {
        return withArray((validToDate, alertResource) -> setField("validToDate", validToDate, alertResource), validToDates);
    }
}
