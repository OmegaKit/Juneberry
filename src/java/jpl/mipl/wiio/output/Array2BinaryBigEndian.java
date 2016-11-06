package jpl.mipl.wiio.output;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

/**
 * @author Xing
 */
public class Array2BinaryBigEndian extends Array2BinaryAbstract {
    public Array2BinaryBigEndian() {
        super(java.nio.ByteOrder.BIG_ENDIAN);
    }
}
