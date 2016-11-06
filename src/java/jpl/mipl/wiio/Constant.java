package jpl.mipl.wiio;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

/**
 * @author Xing
 */
public class Constant {
    public static final String APP_NAME = "wiio";
    public static final String APP_VERSION = "0.7.1";

    public static final String NOTE = "note";

    public static final String SIZE = "size";
    //public static final String WIDTH = "width";
    //public static final String HEIGHT = "height";

    // to support raster as numeric array
    public static final String SHAPE = "shape";
    public static final String TYPE = "type";

    // entity names used in webified imageio model
    public static final String LEAF_IMAGE = "image";
    public static final String NODE_RASTER = "raster";
    public static final String NODE_RESIZED = "resized";
    public static final String NODE_STRETCHED = "stretched";

    // max size in bytes allowed for an array to be converted into json
    public static final String SYS_PROPERTY_OUTPUT_ARRAY2JSON_MAX = "wiio.output.array2json.max";
    public static final int OUTPUT_ARRAY2JSON_MAX = 8388608; // = 2^23
}
