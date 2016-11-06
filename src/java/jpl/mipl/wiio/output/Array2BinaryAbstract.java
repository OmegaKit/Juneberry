package jpl.mipl.wiio.output;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.Map;

import the.treevotee.output.Output;
import the.treevotee.output.Writer;
import the.treevotee.output.WriterException;

/**
 * @author Xing
 */
public abstract class Array2BinaryAbstract extends Writer {

    private String mimeType = "application/octet-stream";

    private ByteOrder byteOrder = null;

    public Array2BinaryAbstract(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    private byte[] convert_double_array(double[] array, ByteOrder byteOrder) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(array.length*8);
        byteBuffer.order(byteOrder);
        for (int i=0; i<array.length; i++) {
            byteBuffer.putDouble(array[i]);
        }
        return byteBuffer.array();
    }

    private byte[] convert_float_array(float[] array, ByteOrder byteOrder) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(array.length*4);
        byteBuffer.order(byteOrder);
        for (int i=0; i<array.length; i++) {
            byteBuffer.putFloat(array[i]);
        }
        return byteBuffer.array();
    }

    private byte[] convert_int_array(int[] array, ByteOrder byteOrder) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(array.length*4);
        byteBuffer.order(byteOrder);
        for (int i=0; i<array.length; i++) {
            byteBuffer.putInt(array[i]);
        }
        return byteBuffer.array();
    }

    private byte[] convert_int_array_as_short(int[] array, ByteOrder byteOrder) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(array.length*2);
        byteBuffer.order(byteOrder);
        for (int i=0; i<array.length; i++) {
            byteBuffer.putShort((short)array[i]);
        }
        return byteBuffer.array();
    }

    private byte[] convert_int_array_as_byte(int[] array) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(array.length);
        //byteBuffer.order(byteOrder); //byte order is irrelevant here
        for (int i=0; i<array.length; i++) {
            byteBuffer.put((byte)array[i]);
        }
        return byteBuffer.array();
    }

    public Output write(Map map) throws WriterException {
        // type
        String type = (String)map.get(jpl.mipl.wiio.Constant.TYPE);
        if (type == null)
            throw new WriterException("internal inconsistency: type is not passed to this writer in map");

        // shape
        // not needed

        // array
        Object obj = map.get(the.treevotee.Constant.DATA);
        if (obj == null)
            throw new WriterException("internal inconsistency: array object is null");
        Class ofArray = obj.getClass().getComponentType();
        if (ofArray == null)
            throw new WriterException("internal inconsistency: object passed is not an array");
        String arrayType = ofArray.getName();

        byte[] bytes = null;
        if (arrayType.equals("double")) {
            if (!type.equals("float64"))
                throw new WriterException("internal inconsistency: type is not float64");
            bytes = convert_double_array((double[])obj, this.byteOrder);
        } else if (arrayType.equals("float")) {
            if (!type.equals("float32"))
                throw new WriterException("internal inconsistency: type is not float32");
            bytes = convert_float_array((float[])obj, this.byteOrder);
        } else if (arrayType.equals("int")) {
            if (type.equals("int32")) {
                bytes = convert_int_array((int[])obj, this.byteOrder);
            } else if (type.equals("int16") || type.equals("uint16")) {
                bytes = convert_int_array_as_short((int[])obj, this.byteOrder);
            // in fact, there would be no int8 as mapped by RasterExposer.java
            //} else if (type.equals("int8") || type.equals("uint8")) {
            } else if (type.equals("uint8")) {
                bytes = convert_int_array_as_byte((int[])obj);
            } else {
                throw new WriterException("internal inconsistency: type "+type+"is not supported");
            }
        } else {
            throw new WriterException("do not know how to convert for array type "+arrayType+" and type "+type);
        }

        return new Output(bytes, this.mimeType);
    }
}
