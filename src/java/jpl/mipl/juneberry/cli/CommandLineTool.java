package jpl.mipl.juneberry.cli;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import jpl.mipl.juneberry.Constant;

import jpl.mipl.juneberry.store.read.ReaderClassFactory;
import jpl.mipl.juneberry.output.WriterClassFactory;

import jpl.mipl.juneberry.cache.LocalEhcache;

import the.treevotee.W10n;

import the.treevotee.cache.Cache;
import the.treevotee.cache.KeyMaker;

import java.util.Date;

/**
 *
 * @author Xing
 */
public class CommandLineTool {

    public CommandLineTool() {}

    public static void main(String[] args) throws Exception {

        // check where important classes are loaded from
        // ref: http://stackoverflow.com/questions/1983839/determine-which-jar-file-a-class-is-from
        System.err.println("Check where important classes are loaded from");
        Class klass = CommandLineTool.class;
        System.err.println(klass.getResource("/javax/imageio/ImageIO.class"));
        System.err.println(klass.getResource("/javax/media/jai/JAI.class"));

        W10n w10n = new W10n();
        w10n.application += "/"+Constant.APP_NAME+"-"+Constant.APP_VERSION;

        Cache readCache = new LocalEhcache(Constant.READ_CACHE);
        Cache outputCache = new LocalEhcache(Constant.OUTPUT_CACHE);
        KeyMaker keyMaker = null; // to use treevotee's key maker

        long t0 = System.currentTimeMillis();
        long t = t0;

        readCache.init();
        outputCache.init();

        the.treevotee.cli.CommandLineTool commandLineTool =
            new the.treevotee.cli.CommandLineTool(
                new ReaderClassFactory(),
                new WriterClassFactory(),
                w10n,
                null, outputCache, keyMaker
                //readCache, outputCache, keyMaker
                //null, null, null
            );

    String[] namesOfKeyedArgsForStoreReaderConfig = new String[]{};

    //for (int i=0; i<100; i++) {
    for (int i=0; i<1; i++) {
        t = System.currentTimeMillis(); System.err.println("prepare: "+(t-t0)); t0 = t;
        commandLineTool.doit(args, namesOfKeyedArgsForStoreReaderConfig);
        t = System.currentTimeMillis(); System.err.println("commandLineTool.doit: "+(t-t0)); t0 = t;
    }

        outputCache.end();
        readCache.end();
    }
}
