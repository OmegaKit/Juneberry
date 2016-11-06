package jpl.mipl.wiio.output;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import the.treevotee.Constant;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import java.util.Map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Iterator;

import the.treevotee.output.Output;
import the.treevotee.output.Writer;
import the.treevotee.output.WriterException;

/**
 * @author Xing
 */
public class Jpeg extends Writer {

    private Iterator writers = ImageIO.getImageWritersByFormatName("jpeg");
    private ImageWriter writer = (ImageWriter)writers.next();

    private String mimeType = "image/jpeg";

    public Jpeg() {}

    public Output write(Map map) throws WriterException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);
            BufferedImage bi = (BufferedImage)map.get(Constant.DATA);
            //System.out.println("width is "+bi.getWidth());
            if (bi == null) {
                throw new WriterException("internal inconsistency.");
            }
            writer.write(bi);
            ios.flush();
            writer.dispose();
            ios.close();
            //System.out.println("buffer size is "+baos.size());
            //byte[] bytes = baos.toByteArray();
            //System.out.println("length is "+bytes.length);
            return new Output(baos.toByteArray(),this.mimeType);
        } catch (IOException ioe) {
            throw new WriterException(ioe);
        }
    }
}
