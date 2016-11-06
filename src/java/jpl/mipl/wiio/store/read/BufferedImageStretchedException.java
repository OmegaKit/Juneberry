package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.io.Serializable;

/**
 * @author Xing
 */

public class BufferedImageStretchedException extends Exception implements Serializable {

    private static final long serialVersionUID = 2187529376306177797L;

    public BufferedImageStretchedException() {
        super();
    }

    public BufferedImageStretchedException(String message) {
        super(message);
    }

    public BufferedImageStretchedException(String message, Throwable cause) {
        super(message, cause);
    }

    public BufferedImageStretchedException(Throwable cause) {
        super(cause);
    }
}
