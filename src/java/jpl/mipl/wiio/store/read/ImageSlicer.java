package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;

/**
 * @author Xing
 */

public class ImageSlicer {

    public ImageSlicer() {}

    // parse indexer in the form of (x,y)width*height into [x,y,w,h]
    // the multiplier can be '*' or 'x'
    private int[] parse_indexer_tile(String indexer) throws ImageSlicerException {
        //int ints = new int[4];
      try {
        // get indices to figure out x and y
        int idxLeftParenthesis = indexer.indexOf("(");
        int idxComma = indexer.indexOf(",");
        int idxRightParenthesis = indexer.indexOf(")");
        if (idxLeftParenthesis != 0 || idxComma == -1 || idxRightParenthesis == -1)
            throw new ImageSlicerException("Invalid indexer format: unknown (x,y)");
        // get indices to figure out width and height
        int idxAsterisk = indexer.indexOf("*");
        int idxLowerCaseX = indexer.indexOf("x");
        if (idxAsterisk == -1 && idxLowerCaseX == -1)
            throw new ImageSlicerException("Invalid indexer format: unknown width*height");
        int idxMultiplier = idxLowerCaseX;
        if (idxMultiplier == -1)
            idxMultiplier = idxAsterisk;

        int x = Integer.parseInt(indexer.substring(idxLeftParenthesis+1, idxComma));
        int y = Integer.parseInt(indexer.substring(idxComma+1, idxRightParenthesis));
        int w = Integer.parseInt(indexer.substring(idxRightParenthesis+1, idxMultiplier));
        int h = Integer.parseInt(indexer.substring(idxMultiplier+1));
        return new int[] {x, y, w, h};
      } catch (java.lang.NumberFormatException nfe) {
        throw new ImageSlicerException("Invalid indexer format: "+nfe);
      }
    }

    // parse indexer in the form of start:end,start:end into [x,y,w,h]
    private int[] parse_indexer_range(String indexer) throws ImageSlicerException {
        int idx1stColon = indexer.indexOf(":");
        int idxComma = indexer.indexOf(",");
        int idx2ndColon = indexer.lastIndexOf(":");
        if (idx1stColon == -1 || idxComma == -1 || idx2ndColon == -1)
            throw new ImageSlicerException("Unknown indexer format");
        int x0 = Integer.parseInt(indexer.substring(0,idx1stColon));
        int x1 = Integer.parseInt(indexer.substring(idx1stColon+1,idxComma));
        int y0 = Integer.parseInt(indexer.substring(idxComma+1,idx2ndColon));
        int y1 = Integer.parseInt(indexer.substring(idx2ndColon+1));
        return new int[] {x0, y0, x1-x0, y1-y0};
    }

    private int[] parse_indexer(String indexer) throws ImageSlicerException {
        if (indexer.startsWith("("))
            return this.parse_indexer_tile(indexer);
        return this.parse_indexer_range(indexer);
    }

    public BufferedImage slice(BufferedImage bufferedImage, String indexer) throws ImageSlicerException {
        int x = -1, y = -1, w = -1, h = -1;
        if (!indexer.equals("")) {
            int[] tmp = this.parse_indexer(indexer);
            x = tmp[0]; y = tmp[1]; w = tmp[2]; h = tmp[3];
        }
        if (x == -1)
            return bufferedImage;
        return bufferedImage.getSubimage(x, y, w, h);
    }

    public static void main(String[] args) throws ImageSlicerException {
        if (args.length != 4) {
            System.err.println("Usage: ImageSlicer inPath indexer outputFormat outPath");
            System.exit(1);
        }

        String inPath = args[0];
        String indexer = args[1];
        String outputFormat = args[2];
        String outPath = args[3];

        try {
            BufferedImage bi = ImageIO.read(new File(inPath));
            ImageSlicer slicer = new ImageSlicer();
            bi = slicer.slice(bi, indexer);
            ImageIO.write(bi, outputFormat, new File(outPath));
        } catch (IOException ioe) {
            throw new ImageSlicerException(ioe);
        }
    }
}
