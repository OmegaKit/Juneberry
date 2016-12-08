package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2016, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.io.Serializable;

/**
 * @author Xing
 */
public class CameraModelExposerException extends Exception implements Serializable {


    private static final long serialVersionUID = 8937732468671973995L;

    public CameraModelExposerException() {
        super();
    }

    public CameraModelExposerException(String message) {
        super(message);
    }

    public CameraModelExposerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CameraModelExposerException(Throwable cause) {
        super(cause);
    }
}
