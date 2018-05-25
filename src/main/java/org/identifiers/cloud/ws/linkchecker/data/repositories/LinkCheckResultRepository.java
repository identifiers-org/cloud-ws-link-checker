package org.identifiers.cloud.ws.linkchecker.data.repositories;

import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckResult;
import org.springframework.data.repository.CrudRepository;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.data.repositories
 * Timestamp: 2018-05-25 10:12
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
public interface LinkCheckResultRepository extends CrudRepository<LinkCheckResult, String> {
    // TODO
}