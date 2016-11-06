package jpl.mipl.wiio.output;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

//import the.treevotee.output.WriterClassFactory;

/**
 * @author Xing
 */

public class WriterClassFactory extends the.treevotee.output.WriterClassFactory {

    public Class getWriterClass4meta(String type, String format) {
        if (format == null)
            format = "json";

        Class clazz = null;
        try {
            if (type.startsWith("imageio") && format.equals("html"))
                return this.classLoader.loadClass("jpl.mipl.wiio.output.Html");
        } catch (java.lang.ClassNotFoundException cnde) {
            // do nothing
        }
        if (clazz == null)
            return super.getWriterClass4meta(type, format);
        return clazz;
    }

    public Class getWriterClass4data(String type, String format) {
        Class clazz = null;
        try {
            if (type.startsWith("imageio") && format.equals("gif"))
                return this.classLoader.loadClass("jpl.mipl.wiio.output.Gif");
            if (type.startsWith("imageio") && format.equals("gif.base64"))
                return this.classLoader.loadClass("jpl.mipl.wiio.output.GifBase64");
            if (type.startsWith("imageio") && format.equals("png"))
                return this.classLoader.loadClass("jpl.mipl.wiio.output.Png");
            if (type.startsWith("imageio") && (format.equals("jpeg") || format.equals("jpg")))
                return this.classLoader.loadClass("jpl.mipl.wiio.output.Jpeg");
            if (type.startsWith("imageio") && format.equals("json"))
                return this.classLoader.loadClass("jpl.mipl.wiio.output.Array2Json");
            if (type.startsWith("imageio") && (format.equals("big-endian") || format.equals("be")))
                return this.classLoader.loadClass("jpl.mipl.wiio.output.Array2BinaryBigEndian");
            if (type.startsWith("imageio") && (format.equals("little-endian") || format.equals("le")))
                return this.classLoader.loadClass("jpl.mipl.wiio.output.Array2BinaryLittleEndian");
        } catch (java.lang.ClassNotFoundException cnde) {
            // do nothing
        }
        if (clazz == null)
            return super.getWriterClass4data(type, format);
        return clazz;
    }

    public static void main(String args[]) {
        if (args.length < 1) {
            System.err.println("Usage: WriterClassFactory type identifierType format");
            System.exit(1);
        }

        WriterClassFactory factory = new WriterClassFactory();
        String type = args[0];
        String identifierType = args[1];
        String format = args[2];
        System.out.println("WriterClass is "+factory.getWriterClass(type, identifierType, format));
    }
}
