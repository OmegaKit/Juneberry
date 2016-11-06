package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

/**
 * imageio reader exception
 *
 * @author Xing
 */
public class ImageIOReaderException extends the.treevotee.store.read.ReaderException {

    private static final long serialVersionUID = 2875514787111131977L;

    public ImageIOReaderException() {
        super();
    }

    public ImageIOReaderException(String message) {
        super(message);
    }

    public ImageIOReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageIOReaderException(Throwable cause) {
        super(cause);
    }
}
