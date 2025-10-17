package com.poc.baraka.common;

/**
 * @author Waqas Ahmed
 */

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class IdGenerator {

    private final AtomicLong counter = new AtomicLong(0); // start from 1

    /**
     * Generate a unique long ID within this Spring Boot session.
     */
    public long nextId() {
        return counter.getAndIncrement();
    }
}

