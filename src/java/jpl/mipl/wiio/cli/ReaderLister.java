package jpl.mipl.wiio.cli;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import javax.imageio.spi.IIORegistry;

import java.util.Iterator;

/**
 *
 * @author Xing
 */
public class ReaderLister {

    public ReaderLister() {
    }

    private void list_reader_by_format_name(String name) {
        Iterator iterator = ImageIO.getImageReadersByFormatName(name);
        while (iterator.hasNext()) {
            ImageReader reader = (ImageReader)iterator.next();
            System.out.println("   reader: "+reader.getClass().getName());
            System.out.println("           canReadRaster="+reader.canReadRaster());
        }
    }

    private void list_reader_by_mime_type(String type) {
        Iterator iterator = ImageIO.getImageReadersByMIMEType(type);
        while (iterator.hasNext()) {
            ImageReader reader = (ImageReader)iterator.next();
            System.out.println("   reader: "+reader.getClass().getName());
            System.out.println("           canReadRaster="+reader.canReadRaster());
        }
    }

    public void list() {
        String[] formatNames = ImageIO.getReaderFormatNames();
        for (int i=0; i<formatNames.length; i++) {
            System.out.println("format: "+formatNames[i]);
            this.list_reader_by_format_name(formatNames[i]);
        }
        String[] mimeTypes = ImageIO.getReaderMIMETypes();
        for (int i=0; i<mimeTypes.length; i++) {
            System.out.println("mimeType: "+mimeTypes[i]);
            this.list_reader_by_mime_type(mimeTypes[i]);
        }
    }

    public static void main(String[] args) throws Exception {
        ReaderLister lister = new ReaderLister();
        lister.list();
    }
}
