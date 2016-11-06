package jpl.mipl.wiio.cli;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import jpl.mipl.wiio.store.read.ReaderClassFactory;
import jpl.mipl.wiio.output.WriterClassFactory;
import jpl.mipl.wiio.Constant;

//import jpl.mipl.wiio.cache.LocalEhcache;

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
        System.err.println(""+klass.getResource("/javax/imageio/ImageIO.class"));
        System.err.println(""+klass.getResource("/javax/media/jai/JAI.class"));

        W10n w10n = new W10n();
        w10n.application += "/"+Constant.APP_NAME+"-"+Constant.APP_VERSION;

        //Cache readCache = new LocalEhcache("sampleCache1");
        //Cache outputCache = new LocalEhcache("sampleCache2");
        //KeyMaker keyMaker = null; // to use treevotee's key maker

        long t0 = System.currentTimeMillis();
        long t = t0;

        //readCache.init();
        //outputCache.init();

        the.treevotee.cli.CommandLineTool commandLineTool =
            new the.treevotee.cli.CommandLineTool(
                new ReaderClassFactory(),
                new WriterClassFactory(),
                w10n,
                null, null, null
                //readCache, outputCache, keyMaker
            );

    String[] namesOfKeyedArgsForStoreReaderConfig = new String[]{};

    //for (int i=0; i<100; i++) {
    for (int i=0; i<1; i++) {
        t = System.currentTimeMillis(); System.err.println("prepare: "+(t-t0)); t0 = t;
        commandLineTool.doit(args, namesOfKeyedArgsForStoreReaderConfig);
        t = System.currentTimeMillis(); System.err.println("commandLineTool.doit: "+(t-t0)); t0 = t;
    }

        //outputCache.end();
        //readCache.end();
    }
}
