package jpl.mipl.juneberry.store.read;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

/**
 * edr isis exception
 *
 * @author Xing
 */
public class EdrIsisException extends jpl.mipl.wiio.store.read.ImageIOReaderException {

    private static final long serialVersionUID = 2871627421179695432L;

    public EdrIsisException() {
        super();
    }

    public EdrIsisException(String message) {
        super(message);
    }

    public EdrIsisException(String message, Throwable cause) {
        super(message, cause);
    }

    public EdrIsisException(Throwable cause) {
        super(cause);
    }
}
