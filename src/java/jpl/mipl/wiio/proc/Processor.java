package jpl.mipl.wiio.proc;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.awt.image.BufferedImage;

import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Map;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Xing
 */
public abstract class Processor {

    // processor types
    public static String TYPE_EXAMPLE = "example";
    public static String TYPE_STRETCH = "stretch";
    //public static String TYPE_RESIZE = "resize";

    public Processor() {}

    // get a list of methods (algorithms) implemented for this processor
    public abstract ArrayList get_list() throws ProcessorException;

    // given a json text or its url, return a map
    public final Map get_usage(String str) throws ProcessorException {
        // if not json string, urlString is assumed
        if (!str.startsWith("{")) {
            String urlString = str;
            str = this.get_resource(urlString);
        }
        Map map = (Map)JSONValue.parse(str);
        return map;
    }

    public abstract BufferedImage process(BufferedImage bi, Map map) throws ProcessorException;

    // str is a json text or its url
    public final BufferedImage process(BufferedImage bi, String str) throws ProcessorException {
        return this.process(bi, this.get_usage(str));
    }

    private String get_resource(String urlString) throws ProcessorException {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException mue) {
            throw new ProcessorException(mue);
        }
        return this.get_resource(url);
    }

    private String get_resource(URL url) throws ProcessorException {
        BufferedReader br = null;
        String content = "";
        try {
            br = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = null;
            while ((line = br.readLine()) != null)
                content += line;
        } catch (FileNotFoundException fnfe) {
            //System.err.println("Failed to open stream to URL: "+fnfe);
            throw new ProcessorException(fnfe);
        } catch (IOException ioe) {
            //System.err.println("Error reading URL content: "+ioe);
            throw new ProcessorException(ioe);
        }
        if (br != null)
            try {br.close();} catch (IOException ioe) {}
        return content;
    }

}
