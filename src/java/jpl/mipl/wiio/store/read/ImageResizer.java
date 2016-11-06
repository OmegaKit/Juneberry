package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

//import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;

/**
 * @author Xing
 */

// 20110630, xing, reference:
// http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
// In summary:
// do not use Image.getScaledInstance()
// do use Graphics.drawImage()

public class ImageResizer {

    public ImageResizer() {}

    /*
    public RenderedImage resize(RenderedImage bi, int width, int height) {
        return null;
    }
    */

    public BufferedImage resize(BufferedImage bi, int width, int height) {
        // For now, we prefer speed than quality.
        // VALUE_INTERPOLATION_NEAREST_NEIGHBOR: fast, low quality
        // VALUE_INTERPOLATION_BILINEAR: slow, high quality
        // VALUE_INTERPOLATION_BICUBIC: slower, higher quality
        return this.resize(bi, width, height, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }

    public BufferedImage resize(BufferedImage bi, int width, int height, Object hint) {
        int type = (bi.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage tmp = new BufferedImage(width, height, type);
        Graphics2D g2 = tmp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
        g2.drawImage(bi, 0, 0, width, height, null);
        g2.dispose();
        return tmp;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 5) {
            System.err.println("Usage: ImageResizer inPath width height outputFormat outPath");
            System.exit(1);
        }

        String inPath = args[0];
        int width = Integer.parseInt(args[1]);
        int height = Integer.parseInt(args[2]);
        String outputFormat = args[3];
        String outPath = args[4];

        BufferedImage bi = ImageIO.read(new File(inPath));

        ImageResizer resizer = new ImageResizer();
        bi = resizer.resize(bi, width, height);

        ImageIO.write(bi, outputFormat, new File(outPath));
    }
}
