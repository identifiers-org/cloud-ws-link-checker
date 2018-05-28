package org.identifiers.cloud.ws.linkchecker.strategies;

import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckResult;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.strategies
 * Timestamp: 2018-05-28 8:24
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * This is the interface of Link Checking strategies
 */
public interface LinkChecker {

    LinkCheckerReport check(String url);
}
