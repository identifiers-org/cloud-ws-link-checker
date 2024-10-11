package org.identifiers.cloud.ws.linkchecker.strategies;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.strategies
 * Timestamp: 2018-05-28 8:29
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * Report built by a link checking strategy.
 */
@Getter @Setter @Accessors(chain = true)
public class LinkCheckerReport implements Serializable {
    // Checked URL
    private String url;
    // UTC time stamp when the URL was checked
    private Timestamp timestamp;
    // Returned HTTP Status
    private int httpStatus;
    // Checking strategy URL status evaluation. This is an assessment of the checked URL, on whether it is considered to
    // lead to a non-error resource, and it is calculated by the link checking strategy used.
    private boolean urlAssessmentOk = false;
}
