package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.io.Serializable;

/**
 * @author Xing
 */
public class ImageExposerException extends Exception implements Serializable {

    private static final long serialVersionUID = 1178019791236302796L;

    public ImageExposerException() {
        super();
    }

    public ImageExposerException(String message) {
        super(message);
    }

    public ImageExposerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageExposerException(Throwable cause) {
        super(cause);
    }
}
