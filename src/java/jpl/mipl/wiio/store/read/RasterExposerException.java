package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.io.Serializable;

/**
 * @author Xing
 */

public class RasterExposerException extends Exception implements Serializable {

    private static final long serialVersionUID = 2789919926855024318L;

    public RasterExposerException() {
        super();
    }

    public RasterExposerException(String message) {
        super(message);
    }

    public RasterExposerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RasterExposerException(Throwable cause) {
        super(cause);
    }
}
