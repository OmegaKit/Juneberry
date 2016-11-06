package jpl.mipl.juneberry.store.read;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import javax.imageio.spi.IIORegistry;

import jpl.mipl.io.plugins.PDSImageReaderSpi;
import jpl.mipl.io.plugins.VicarImageReaderSpi;
import jpl.mipl.io.plugins.ISISImageReaderSpi;
// 20110918, xing, not to have vicario handle fits for now
//import jpl.mipl.io.plugins.FITSImageReaderSpi;

/**
 * @author Xing
 */
public class RasterExposer extends jpl.mipl.wiio.store.read.RasterExposer {

    static {
        IIORegistry.getDefaultInstance().registerServiceProvider(new PDSImageReaderSpi());
        IIORegistry.getDefaultInstance().registerServiceProvider(new VicarImageReaderSpi());
        IIORegistry.getDefaultInstance().registerServiceProvider(new ISISImageReaderSpi());
    }

    public static void main(String[] args) throws jpl.mipl.wiio.store.read.RasterExposerException {
        jpl.mipl.wiio.store.read.RasterExposer.main(args);
    }

}
