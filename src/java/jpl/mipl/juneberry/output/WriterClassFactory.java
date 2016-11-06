package jpl.mipl.juneberry.output;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

//import jpl.mipl.wiio.output.WriterClassFactory;

/**
 * @author Xing
 */

public class WriterClassFactory extends jpl.mipl.wiio.output.WriterClassFactory {

    public Class getWriterClass4meta(String type, String format) {
        if (format == null)
            format = "json";

        Class clazz = null;
        try {
            if (type.equals("imageio.vicario") || type.equals("imageio.fits") || type.equals("x.jpl.edr")) {
                if (format.equals("html"))
                    return this.classLoader.loadClass("jpl.mipl.wiio.output.Html");
                    //return this.classLoader.loadClass("jpl.mipl.juneberry.output.Html");
            }
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
            if (type.equals("x.jpl.edr") && format.equals("gif"))
                clazz = this.classLoader.loadClass("jpl.mipl.wiio.output.Gif");
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
