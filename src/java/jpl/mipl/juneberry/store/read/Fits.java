package jpl.mipl.juneberry.store.read;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import javax.imageio.spi.IIORegistry;

import edu.jhu.pha.sdss.fits.imageio.FITSReaderSpi;

import java.util.Map;

import java.io.File;
import java.io.IOException;

import jpl.mipl.wiio.store.read.ImageIOReader;

/**
 * @author Xing
 */
public class Fits extends ImageIOReader {
    static {
        IIORegistry.getDefaultInstance().registerServiceProvider(new FITSReaderSpi());
    }

    private String PLUGIN_NAME = "edu.jhu.pha.sdss.fits.imageio.FITSReader";

    public Fits(String sid, String path, Map config) throws FitsException {
        super(sid, path, config);

        try {
            File file = new File(this.path);
            if (!file.exists())
                throw new IOException("file does not exist: "+this.path);
            Iterator iter = ImageIO.getImageReadersByFormatName("fits");
            while (iter.hasNext()) {
                this.reader = (ImageReader)iter.next();
                if (this.reader.getClass().getName().equals(PLUGIN_NAME)) {
                    break;
                } else {
                    this.reader = null;
                }
            }
            if (this.reader == null)
                throw new IOException("missing imageio plugin "+PLUGIN_NAME);
            ImageInputStream iis = ImageIO.createImageInputStream(file);
            if (iis == null)
                throw new IOException("failed to create image input stream for "+this.path);
            this.reader.setInput(iis);
        //} catch (java.util.NoSuchElementException nsee) {
        //    throw new FitsException(nsee+": there seems no imageio plugin available for "+this.path);
        } catch (IOException ioe) {
            throw new FitsException(ioe);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FitsException(e);
        }
    }

    // this is unnecessary
    public void dispose() {
        super.dispose();
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            System.err.println("Usage: Fits sid path identifier");
            System.exit(1);
        }

        String sid = args[0];
        String path = args[1];
        String identifier = args[2];

        Map config = null;
        Fits reader = new Fits(sid, path, config);
        Map obj = reader.get(identifier);
        System.out.print(org.json.simple.JSONValue.toJSONString(obj));
    }
}
