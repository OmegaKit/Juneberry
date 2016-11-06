package jpl.mipl.juneberry.proc;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

//import jpl.mipl.wiio.proc.ProcessorClassFactory;
import jpl.mipl.wiio.proc.Processor;

/**
 * @author Xing
 */

public class ProcessorClassFactory extends jpl.mipl.wiio.proc.ProcessorClassFactory {

    public Class getClass(String type) {
        Class clazz = null;
        try {
            // todo?
            //if (type.equals(Processor.TYPE_RESIZE))
            //    return this.classLoader.loadClass("jpl.mipl.juneberry.proc.Resizer");
            if (type.equals(Processor.TYPE_STRETCH))
                return this.classLoader.loadClass("jpl.mipl.juneberry.proc.Stretcher");
            clazz = super.getClass(type);
        } catch (java.lang.ClassNotFoundException cnde) {
            // do nothing
        }
        return clazz;
    }

    public static void main(String args[]) {
        if (args.length < 1) {
            System.err.println("Usage: ProcessorClassFactory type");
            System.exit(1);
        }

        ProcessorClassFactory factory = new ProcessorClassFactory();
        String type = args[0];
        System.out.println("Class is "+factory.getClass(type));
    }
}
