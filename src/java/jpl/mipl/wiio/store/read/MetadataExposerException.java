package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.io.Serializable;

/**
 * @author Xing
 */
public class MetadataExposerException extends Exception implements Serializable {

    private static final long serialVersionUID = 3169328151103731690L;

    public MetadataExposerException() {
        super();
    }

    public MetadataExposerException(String message) {
        super(message);
    }

    public MetadataExposerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetadataExposerException(Throwable cause) {
        super(cause);
    }
}
