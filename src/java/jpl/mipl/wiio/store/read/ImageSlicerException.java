package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.io.Serializable;

/**
 * @author Xing
 */

public class ImageSlicerException extends Exception implements Serializable {

  public ImageSlicerException() {
    super();
  }

  public ImageSlicerException(String message) {
    super(message);
  }

  public ImageSlicerException(String message, Throwable cause) {
    super(message, cause);
  }

  public ImageSlicerException(Throwable cause) {
    super(cause);
  }

}
