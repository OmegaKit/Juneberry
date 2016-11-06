package jpl.mipl.juneberry.store.read;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

/**
 * fits exception
 *
 * @author Xing
 */
public class FitsException extends jpl.mipl.wiio.store.read.ImageIOReaderException {

    private static final long serialVersionUID = 2848927023218743210L;

    public FitsException() {
        super();
    }

    public FitsException(String message) {
        super(message);
    }

    public FitsException(String message, Throwable cause) {
        super(message, cause);
    }

    public FitsException(Throwable cause) {
        super(cause);
    }
}
