package jpl.mipl.wiio.output;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import the.treevotee.output.Output;
import the.treevotee.output.Writer;
import the.treevotee.output.WriterException;

/**
 * @author Xing
 */
public class Array2Json extends Writer {

    private String mimeType = "application/json";

    // cap size to maxSize bytes, so that this writer will not choke.
    private int maxSize = Integer.getInteger(jpl.mipl.wiio.Constant.SYS_PROPERTY_OUTPUT_ARRAY2JSON_MAX, jpl.mipl.wiio.Constant.OUTPUT_ARRAY2JSON_MAX).intValue();

    public Array2Json() {}

    // dimension of size 1 will be squeezed, so List or Number can be returned.
    private Object convert_double_array(double[] array, int lines, int samples, int bands) throws WriterException {
        int sizeInBytes = array.length * 8; //java double has 8 bytes
        if (sizeInBytes > this.maxSize)
            throw new WriterException("size "+sizeInBytes+" exceeds "+maxSize+" bytes. please subsample using an indexer.");

        List lineList = new ArrayList(lines);
        for (int i=0; i<lines; i++) {
            List sampleList = new ArrayList(samples);
            for (int j=0; j<samples; j++) {
                List bandList = new ArrayList(bands);
                for (int k=0; k<bands; k++) {
                    int idx = k + (j + i*samples)*bands;
                    bandList.add(new Double(array[idx]));
                }
                sampleList.add((bandList.size() == 1) ? bandList.get(0) : bandList);
            }
            lineList.add((sampleList.size() == 1) ? sampleList.get(0) : sampleList);
        }
        return (lineList.size() == 1) ? lineList.get(0) : lineList;
    }

    // dimension of size 1 will be squeezed, so List or Number can be returned.
    private Object convert_float_array(float[] array, int lines, int samples, int bands) throws WriterException {
        int sizeInBytes = array.length * 4; //java float has 4 bytes
        if (sizeInBytes > this.maxSize)
            throw new WriterException("size "+sizeInBytes+" exceeds "+maxSize+" bytes. please subsample using an indexer.");

        List lineList = new ArrayList(lines);
        for (int i=0; i<lines; i++) {
            List sampleList = new ArrayList(samples);
            for (int j=0; j<samples; j++) {
                List bandList = new ArrayList(bands);
                for (int k=0; k<bands; k++) {
                    int idx = k + (j + i*samples)*bands;
                    bandList.add(new Float(array[idx]));
                }
                sampleList.add((bandList.size() == 1) ? bandList.get(0) : bandList);
            }
            lineList.add((sampleList.size() == 1) ? sampleList.get(0) : sampleList);
        }
        return (lineList.size() == 1) ? lineList.get(0) : lineList;
    }

    // dimension of size 1 will be squeezed, so List or Number can be returned.
    private Object convert_int_array(int[] array, int lines, int samples, int bands) throws WriterException {
        int sizeInBytes = array.length * 4; //java int has 4 bytes
        if (sizeInBytes > this.maxSize)
            throw new WriterException("size "+sizeInBytes+" exceeds "+maxSize+" bytes. please subsample using an indexer.");

        List lineList = new ArrayList(lines);
        for (int i=0; i<lines; i++) {
            List sampleList = new ArrayList(samples);
            for (int j=0; j<samples; j++) {
                List bandList = new ArrayList(bands);
                for (int k=0; k<bands; k++) {
                    int idx = k + (j + i*samples)*bands;
                    bandList.add(new Integer(array[idx]));
                }
                sampleList.add((bandList.size() == 1) ? bandList.get(0) : bandList);
            }
            lineList.add((sampleList.size() == 1) ? sampleList.get(0) : sampleList);
        }
        return (lineList.size() == 1) ? lineList.get(0) : lineList;
    }

    // java short is a 16-bit signed two's complement integer
    // java.awt.image.Raster does not support the following
    //short[] shorts = raster.getPixels(x, y, width, height, iArray);
    // so int is used and later casted to short
    //
    // dimension of size 1 will be squeezed, so List or Number can be returned.
    private Object convert_int_array_as_short(int[] array, int lines, int samples, int bands) throws WriterException {
        int sizeInBytes = array.length * 2; //jave short has 2 bytes
        if (sizeInBytes > this.maxSize)
            throw new WriterException("size "+sizeInBytes+" exceeds "+maxSize+" bytes. please subsample using an indexer.");

        List lineList = new ArrayList(lines);
        for (int i=0; i<lines; i++) {
            List sampleList = new ArrayList(samples);
            for (int j=0; j<samples; j++) {
                List bandList = new ArrayList(bands);
                for (int k=0; k<bands; k++) {
                    int idx = k + (j + i*samples)*bands;
                    // it should be safe to cast to short here, 
                    // because this method get called only if buffer type is TYPE_SHORT
                    bandList.add(new Short((short)array[idx]));
                }
                sampleList.add((bandList.size() == 1) ? bandList.get(0) : bandList);
            }
            lineList.add((sampleList.size() == 1) ? sampleList.get(0) : sampleList);
        }
        return (lineList.size() == 1) ? lineList.get(0) : lineList;
    }

    // java byte is an 8-bit signed two's complement integer
    // java.awt.image.Raster does not support the following
    //byte[] bytes = raster.getPixels(x, y, width, height, iArray);
    // so int is used and later casted to byte
    //
    // dimension of size 1 will be squeezed, so List or Number can be returned.
    private Object convert_int_array_as_byte(int[] array, int lines, int samples, int bands) throws WriterException {
        int sizeInBytes = array.length; //java byte is a byte
        if (sizeInBytes > this.maxSize)
            throw new WriterException("size "+sizeInBytes+" exceeds "+maxSize+" bytes. please subsample using an indexer.");

        List lineList = new ArrayList(lines);
        for (int i=0; i<lines; i++) {
            List sampleList = new ArrayList(samples);
            for (int j=0; j<samples; j++) {
                List bandList = new ArrayList(bands);
                for (int k=0; k<bands; k++) {
                    int idx = k + (j + i*samples)*bands;
                    // it should be safe to cast to byte here, 
                    // because this method get called only if buffer type is TYPE_BYTE
                    bandList.add(new Byte((byte)array[idx]));
                }
                sampleList.add((bandList.size() == 1) ? bandList.get(0) : bandList);
            }
            lineList.add((sampleList.size() == 1) ? sampleList.get(0) : sampleList);
        }
        return (lineList.size() == 1) ? lineList.get(0) : lineList;
    }

    public Output write(Map map) throws WriterException {
        // type
        String type = (String)map.get(jpl.mipl.wiio.Constant.TYPE);
        if (type == null)
            throw new WriterException("internal inconsistency: type is not passed to this writer in map");

        // shape
        int[] shape = (int[])map.get(jpl.mipl.wiio.Constant.SHAPE);
        if (shape == null)
            throw new WriterException("internal inconsistency: shape is not passed to this writer in map");
        int lines = shape[0];
        int samples = shape[1];
        int bands = shape[2];
        map.remove(jpl.mipl.wiio.Constant.SHAPE); // no need to show this shape

        // array
        Object obj = map.get(the.treevotee.Constant.DATA);
        if (obj == null)
            throw new WriterException("internal inconsistency: array object is null");
        Class ofArray = obj.getClass().getComponentType();
        if (ofArray == null)
            throw new WriterException("internal inconsistency: object passed is not an array");
        String arrayType = ofArray.getName();

      //try {
      //  Thread.sleep(5000);
      //} catch (InterruptedException ie) {
      //  throw new WriterException(ie);
      //}

        Object listOrNumber = null;
        if (arrayType.equals("double")) {
            if (!type.equals("float64"))
                throw new WriterException("internal inconsistency: type is not float64");
            listOrNumber = convert_double_array((double[])obj, lines, samples, bands);
        } else if (arrayType.equals("float")) {
            if (!type.equals("float32"))
                throw new WriterException("internal inconsistency: type is not float32");
            listOrNumber = convert_float_array((float[])obj, lines, samples, bands);
        } else if (arrayType.equals("int")) {
            if (type.equals("int32") || type.equals("uint16")) {
                listOrNumber = convert_int_array((int[])obj, lines, samples, bands);
            } else if (type.equals("int16") || type.equals("uint8")) {
                listOrNumber = convert_int_array_as_short((int[])obj, lines, samples, bands);
            // in fact, there would be no int8 as mapped by RasterExposer.java
            //} else if (type.equals("int8")) {
            //    listOrNumber = convert_int_array_as_byte((int[])obj, lines, samples, bands);
            } else {
                throw new WriterException("internal inconsistency: type "+type+"is not supported");
            }
        } else {
            throw new WriterException("do not know how to convert for array type "+arrayType+" and type "+type);
        }

        map.put(the.treevotee.Constant.DATA, listOrNumber);
        map.put(jpl.mipl.wiio.Constant.TYPE, type);
        return new Output(org.json.simple.JSONValue.toJSONString(map).getBytes(), this.mimeType);
    }
}
