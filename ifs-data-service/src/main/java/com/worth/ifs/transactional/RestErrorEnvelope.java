package com.worth.ifs.transactional;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.worth.ifs.util.CollectionFunctions.simpleToMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;

/**
 *
 */
public class RestErrorEnvelope {

    private List<Error> errors;

    /**
     * For JSON marshalling
     */
    public RestErrorEnvelope() {
    }

    public RestErrorEnvelope(Error error) {
        this(singletonList(error));
    }

    public RestErrorEnvelope(List<Error> errors) {
        this.errors = errors;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public HttpStatus getStatusCode() {

        List<Map.Entry<HttpStatus, Integer>> entries = getHttpStatusCounts();
        return entries.get(0).getKey();
    }

    // TODO DW - INFUND-854 - duplicated code
    private List<Map.Entry<HttpStatus, Integer>> getHttpStatusCounts() {

        Map<HttpStatus, List<Error>> errorsByStatusCode = errors.stream().collect(groupingBy(Error::getStatusCode));
        Map<HttpStatus, Integer> numberOfOccurrancesByStatusCode =
                simpleToMap(new ArrayList<>(errorsByStatusCode.entrySet()), Map.Entry::getKey, entry -> entry.getValue().size());

        List<Map.Entry<HttpStatus, Integer>> entries = new ArrayList<>(numberOfOccurrancesByStatusCode.entrySet());
        entries.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        return entries;
    }
}
