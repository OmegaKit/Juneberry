package jpl.mipl.wiio.proc;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

/**
 * @author Xing
 */

public class ProcessorClassFactory {

    protected static ClassLoader classLoader = ProcessorClassFactory.class.getClassLoader();

    public Class getClass(String type) {
        Class clazz = null;
        try {
            if (type.equals(Processor.TYPE_EXAMPLE))
                clazz = this.classLoader.loadClass("jpl.mipl.wiio.proc.ExampleClass");
        } catch (Exception e) {
            // nothing
            System.err.println("Failed to load class: "+e);
        }
        return clazz;
    }

    public static void main(String args[]) {
        ProcessorClassFactory processorClassFactory = new ProcessorClassFactory();
        String type = args[0];
        System.out.println("Class is "+processorClassFactory.getClass(type));
    }
}
