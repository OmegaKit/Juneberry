package jpl.mipl.juneberry.util;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.io.Serializable;

/**
 * soft link manager exception
 *
 * @author Xing
 */
public class SoftLinkManagerException extends Exception implements Serializable {
  public SoftLinkManagerException() {
    super();
  }

  public SoftLinkManagerException(String message) {
    super(message);
  }

  public SoftLinkManagerException(String message, Throwable cause) {
    super(message, cause);
  }

  public SoftLinkManagerException(Throwable cause) {
    super(cause);
  }

}
