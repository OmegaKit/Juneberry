package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.util.Map;

import java.io.File;
//import java.io.IOException;

/**
 * @author Xing
 */
public class AutoMagic extends ImageIOReader {

    public AutoMagic(String sid, String path, Map config) throws ImageIOReaderException {
        super(sid, path, config);

        ImageInputStream iis = null;
        try {
            File file = new File(this.path);
            if (!file.exists())
                throw new ImageIOReaderException("file does not exist: "+this.path);
            iis = ImageIO.createImageInputStream(new File(this.path));
            if (iis == null)
                throw new ImageIOReaderException("failed to create image input stream for "+this.path);
            Iterator readers = ImageIO.getImageReaders(iis);
            this.reader = (ImageReader)readers.next();
            if (this.reader == null)
                throw new ImageIOReaderException("image reader is null for "+this.path);
            this.reader.setInput(iis);
        } catch (java.util.NoSuchElementException nsee) {
            throw new ImageIOReaderException(nsee+": there seems no imageio plugin available for "+this.path);
        } catch (java.io.IOException ioe) {
            throw new ImageIOReaderException(ioe);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ImageIOReaderException(e);
        }
    }

    public void dispose() {
        super.dispose();
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            System.err.println("Usage: AutoMagic sid path identifier");
            System.exit(1);
        }

        String sid = args[0];
        String path = args[1];
        String identifier = args[2];

        Map config = null;
        AutoMagic reader = new AutoMagic(sid, path, null);
        Map obj = reader.get(identifier);
        System.out.print(org.json.simple.JSONValue.toJSONString(obj));
    }
}
