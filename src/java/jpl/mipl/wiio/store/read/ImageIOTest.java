package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.util.Iterator;

import javax.imageio.ImageIO;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
//import javax.imageio.metadata.IIOMetadata;
//import javax.imageio.metadata.IIOMetadataFormatImpl;

import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

//import org.w3c.dom.Node;
//import org.w3c.dom.NamedNodeMap;

//import java.util.Map;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.ArrayList;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

//import jpl.mipl.wiio.Constant;
//import jpl.mipl.wiio.util.DomXslTransformer;
//import jpl.mipl.wiio.util.DomXslTransformerException;

import java.awt.image.BufferedImage;

//import the.treevotee.Constant;
//import the.treevotee.store.read.Reader;
//import the.treevotee.store.read.ReaderException;

/**
 * @author Xing
 */
public class ImageIOTest {

    public ImageIOTest() {}

    public byte[] convert(String path, int index, String outputFormat)
        throws IOException {

        try {
            long t0 = System.currentTimeMillis(); long t = System.currentTimeMillis();
            // read in
            ImageInputStream iis = ImageIO.createImageInputStream(new File(path));

            Iterator readers = ImageIO.getImageReaders(iis);
            ImageReader reader = (ImageReader)readers.next();
            reader.setInput(iis);

            t = System.currentTimeMillis(); System.err.println("read1: "+(t-t0)); t0 = t;

            BufferedImage bi = reader.read(index, null);

            t = System.currentTimeMillis(); System.err.println("read: "+(t-t0)); t0 = t;

            // write out
            Iterator writers = ImageIO.getImageWritersByFormatName(outputFormat);
            ImageWriter writer = (ImageWriter)writers.next();

            t = System.currentTimeMillis(); System.err.println("convert1: "+(t-t0)); t0 = t;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);

            t = System.currentTimeMillis(); System.err.println("convert2: "+(t-t0)); t0 = t;

            writer.write(bi);

            t = System.currentTimeMillis(); System.err.println("convert3: "+(t-t0)); t0 = t;

            ios.flush();
            writer.dispose();
            ios.close();

            byte[] bytes = baos.toByteArray();

            t = System.currentTimeMillis(); System.err.println("convert: "+(t-t0)); t0 = t;
            return bytes;

        } catch (IOException ioe) {
            throw ioe;
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            System.err.println("Usage: ImageIOTest path index outputFormat");
            System.exit(1);
        }

        String path = args[0];
        System.out.println(path);
        int index = Integer.parseInt(args[1]);
        String outputFormat = args[2];

        ImageIOTest imageIOTest = new ImageIOTest();
      for (int i=0; i<100; i++) {
        byte[] bytes = imageIOTest.convert(path, index, outputFormat);
        System.out.println("size: "+bytes.length);
      }
    }
}
