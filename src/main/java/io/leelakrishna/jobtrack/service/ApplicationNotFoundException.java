package io.leelakrishna.jobtrack.service;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(Long id) {
        super("no application with id " + id);
    }
}
