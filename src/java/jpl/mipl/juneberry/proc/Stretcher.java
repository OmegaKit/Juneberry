package jpl.mipl.juneberry.proc;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

//import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;

import java.util.ArrayList;
import java.util.Map;

//import jpl.mipl.images.Stretch;
//import jpl.mipl.images.StretchMethod;
//import jpl.mipl.images.StretchParameters;
import jpl.mipl.util.image.stretch.cassini.Stretch;
//import jpl.mipl.util.image.stretch.mer.Stretch;
import jpl.mipl.util.image.stretch.StretchException;

import java.net.URL;
import java.net.MalformedURLException;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.io.FileNotFoundException;

//import org.apache.commons.codec.binary.Base64;

import jpl.mipl.wiio.proc.Processor;
import jpl.mipl.wiio.proc.ProcessorException;

/**
 * @author Xing
 */

public class Stretcher extends Processor {

    private Stretch cassiniStretcher = new Stretch();
    private jpl.mipl.util.image.stretch.mer.Stretch merStretcher = new jpl.mipl.util.image.stretch.mer.Stretch();

    private String get_resource(String name) throws ProcessorException {
        InputStream is = null;
        byte[] buf = null;
      try {
        is = this.getClass().getResourceAsStream(name);
        int length = is.available();
        buf = new byte[length];
        is.read(buf);
        //is.close();
      } catch (IOException ioe) {
        throw new ProcessorException(ioe);
      } finally {
        if (is != null) try {
            is.close();
        } catch (Exception ignored) {
            // ignored
        }
      }
        if (buf == null)
            return null;
        return new String(buf);
    }

    public ArrayList get_list() throws ProcessorException {
        String jsonText = this.get_resource("stretcher.json");
        //System.err.println(jsonText);
        return (ArrayList)org.json.simple.JSONValue.parse(jsonText);
    }

    public BufferedImage process(BufferedImage bi, Map map) throws ProcessorException {
        String name = (String)map.get("name");
        if (name == null)
            throw new ProcessorException("Unknown stretcher: "+name);
        Map m = (Map)map.get("input");
      try {
        if (name.equals("cassini_unity")) {
            m.put("method", "unity");
            return cassiniStretcher.stretch(bi, m);
        }
        if (name.equals("cassini_percent")) {
            m.put("method", "percent");
            return cassiniStretcher.stretch(bi, m);
        }
        if (name.equals("cassini_linear")) {
            m.put("method", "linear");
            return cassiniStretcher.stretch(bi, m);
        }
        if (name.equals("mer_extrema")) {
            m.put("method", "extrema");
            RenderedOp rop = JAI.create("AWTImage", bi);
            return merStretcher.stretch(rop, m).getAsBufferedImage();
            //return merStretcher.stretch(bi, m);
        }
        if (name.equals("mer_percent")) {
            m.put("method", "percent");
            RenderedOp rop = JAI.create("AWTImage", bi);
            return merStretcher.stretch(rop, m).getAsBufferedImage();
            //return merStretcher.stretch(bi, m);
        }
      } catch (StretchException se) {
        throw new ProcessorException(se);
      }
        /*
        if (name.equals("mer")) {
            return this.by_marsviewer(bi, map);
        }
        */
        throw new ProcessorException("Unsupport name: "+name);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 5) {
            System.err.println("Usage: Stretcher inPath minPercent maxPercent outputFormat outPath");
            System.exit(1);
        }

        String inPath = args[0];
        float minPercent = Float.parseFloat(args[1]);
        float maxPercent = Float.parseFloat(args[2]);
        String outputFormat = args[3];
        String outPath = args[4];

        String s = "{\"ignoreValues\": null, \"instrumentScaling\": -1, \"range\": [0.001, 0.10000000000000001], \"limit\": {\"minDifference\": 200.0, \"minAbsolute\": 0.0, \"minBelowBg\": 10.0, \"minLowerLimit\": 30.0, \"maxAbsolute\": 4095.0}, \"numBands\": 3, \"method\": \"linear\", \"ignoreRange\": null}";
        Map o = (Map)org.json.simple.JSONValue.parse(s);

        BufferedImage bi = ImageIO.read(new File(inPath));

        Stretcher stretcher = new Stretcher();
        try {
            bi = stretcher.process(bi, o);
        } catch (ProcessorException pe) {
            throw new IOException(pe);
        }

        ImageIO.write(bi, outputFormat, new File(outPath));
    }
}
