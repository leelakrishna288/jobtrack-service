package io.leelakrishna.jobtrack.service;

public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException(String canonicalUrl) {
        super("an application already exists for " + canonicalUrl);
    }
}
