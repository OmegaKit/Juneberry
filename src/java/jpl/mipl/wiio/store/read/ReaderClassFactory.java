package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

//import the.treevotee.store.read.ReaderClassFactory;

/**
 * @author Xing
 */

public class ReaderClassFactory extends the.treevotee.store.read.ReaderClassFactory {

    public Class getReaderClass(String type) {
        //System.err.println("ReaderClassFactory w10n type is "+type);
        Class clazz = null;
        try {
            if (type.equals("imageio"))
                return this.classLoader.loadClass("jpl.mipl.wiio.store.read.AutoMagic");
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
