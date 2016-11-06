package jpl.mipl.wiio.proc;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.io.Serializable;

/**
 * processor exception
 *
 * @author Xing
 */
public class ProcessorException extends Exception implements Serializable {

  public ProcessorException() {
    super();
  }

  public ProcessorException(String message) {
    super(message);
  }

  public ProcessorException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProcessorException(Throwable cause) {
    super(cause);
  }

}
