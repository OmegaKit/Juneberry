package jpl.mipl.juneberry.store.read;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

//import jpl.mipl.wiio.store.read.ReaderClassFactory;

/**
 * @author Xing
 */

public class ReaderClassFactory extends jpl.mipl.wiio.store.read.ReaderClassFactory {

    public Class getReaderClass(String type) {
        //System.err.println("ReaderClassFactory w10n type is "+type);
        Class clazz = null;
        try {
            if (type.equals("imageio.vicario"))
                return this.classLoader.loadClass("jpl.mipl.juneberry.store.read.VicarIO");
            if (type.equals("imageio.fits"))
                return this.classLoader.loadClass("jpl.mipl.juneberry.store.read.Fits");
            if (type.equals("x.jpl.edr"))
                return this.classLoader.loadClass("jpl.mipl.juneberry.store.read.EdrIsis");
            clazz = super.getReaderClass(type);
        } catch (java.lang.ClassNotFoundException cnde) {
            // do nothing
        }
        return clazz;
    }

    public static void main(String args[]) {
        if (args.length < 1) {
            System.err.println("Usage: ReaderClassFactory type");
            System.exit(1);
        }

        ReaderClassFactory factory = new ReaderClassFactory();
        String type = args[0];
        System.out.println("ReaderClass is "+factory.getReaderClass(type));
    }
}
