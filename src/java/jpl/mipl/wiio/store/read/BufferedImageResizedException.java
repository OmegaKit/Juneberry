package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.io.Serializable;

/**
 * @author Xing
 */

public class BufferedImageResizedException extends Exception implements Serializable {

    private static final long serialVersionUID = 2972717485978552607L;

    public BufferedImageResizedException() {
        super();
    }

    public BufferedImageResizedException(String message) {
        super(message);
    }

    public BufferedImageResizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public BufferedImageResizedException(Throwable cause) {
        super(cause);
    }
}
