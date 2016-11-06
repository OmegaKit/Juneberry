package jpl.mipl.juneberry.store.read;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import javax.media.jai.*;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;

import javax.imageio.spi.IIORegistry;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import javax.imageio.IIOImage;
import javax.imageio.metadata.IIOMetadata;

import javax.imageio.ImageReadParam;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import java.lang.ClassNotFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;

import jpl.mipl.wiio.store.read.ImageIOReader;

import jpl.mipl.juneberry.proc.ProcessorClassFactory;
import jpl.mipl.juneberry.util.Util;

import jpl.mipl.io.plugins.PDSImageReaderSpi;
import jpl.mipl.io.plugins.VicarImageReaderSpi;
import jpl.mipl.io.plugins.ISISImageReaderSpi;
// 20110918, xing, not to have vicario handle fits for now
//import jpl.mipl.io.plugins.FITSImageReaderSpi;

import jpl.mipl.io.plugins.PDSImageReadParam;

//import jpl.mipl.juneberry.SimpleConvertBandSelect;
import jpl.mipl.edr.EDR;
import jpl.mipl.edr.reader.IsisEDRReader;
import jpl.mipl.edr.util.EDRConverter;

/**
 * @author Xing
 */
// this is not really an imageio thing!!!
public class EdrIsis extends ImageIOReader {

    private EDR edr = null;

    public EdrIsis(String sid, String path, Map config) throws EdrIsisException {
        super(sid, path, config);

      try {
        IsisEDRReader isisEdrReader = new IsisEDRReader(path);
        isisEdrReader.loadFile();
        this.edr = isisEdrReader.getEDR();
      } catch (IOException ioe) {
        throw new EdrIsisException(ioe);
      }

        this.set_processor_class_factory(new ProcessorClassFactory());
    }

    // this is not really an imageio thing!!!
    public void dispose() {
        // fixme: clean up IsisEDRReader
        // fixme: clean up this.edr

        // it is useless to call parent's dispose()
        //super.dispose();
    }

    private RenderedImage get_rendered_image(int i) throws IOException {
        return null;
    }

    // override get_size() in parent class
    // this is not very efficient!
    protected int[] get_size(int i) throws EdrIsisException {
        return this.edr.get_size();
    }

    // override get_metadata() in parent class
    protected Object get_metadata(int i) throws EdrIsisException {
        //Map map = new HashMap();
        //map.put("warning", "not available due to lack of implementation");
        Map map = this.edr.getProperties();
        return map;
    }

    // override get_buffered_image() in parent class
    protected BufferedImage get_buffered_image(int i) throws EdrIsisException {
        return this.get_buffered_image(i, new int[] {0});
    }

    protected BufferedImage get_buffered_image(int i, int[] bands) throws EdrIsisException {
      try {
        // tmp hack -- start
        short[][] data = this.edr.getBand(bands[0]);
        int w = data[0].length;
        int h = data.length;
        System.out.println("w="+w+",h="+h);
        for (int k=0; k<w; k++) {
        for (int j=0; j<h; j++) {
            System.out.print(data[k][j]+",");
        }
        }
        // tmp hack -- end

        int[] missingLineParms = {-8192, 255, 0, 0};
        //int[] missingLineParms = null;
        int[] nonImageParms = {4112, 50, 40, 100};
        //int[] nonImageParms = null;
        EDRConverter converter = new EDRConverter(this.edr, bands, nonImageParms, missingLineParms);
        return converter.toBufferedImage();
      } catch (IOException ioe) {
        throw new EdrIsisException(ioe);
      }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            System.err.println("Usage: EdrIsis sid path identifier");
            System.exit(1);
        }

        String sid = args[0];
        String path = args[1];
        String identifier = args[2];

        Map config = null;
        EdrIsis reader = new EdrIsis(sid, path, config);
        Map obj = reader.get(identifier);
        System.out.print(org.json.simple.JSONValue.toJSONString(obj));
    }
}
