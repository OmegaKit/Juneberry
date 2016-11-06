package jpl.mipl.juneberry.store.read;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

/**
 * vicario exception
 *
 * @author Xing
 */
public class VicarIOException extends jpl.mipl.wiio.store.read.ImageIOReaderException {

    private static final long serialVersionUID = 2634723625267542964L;

    public VicarIOException() {
        super();
    }

    public VicarIOException(String message) {
        super(message);
    }

    public VicarIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public VicarIOException(Throwable cause) {
        super(cause);
    }
}
