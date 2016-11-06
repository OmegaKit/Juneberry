package jpl.mipl.wiio;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import javax.media.jai.*;

import java.awt.image.*;
import java.awt.image.renderable.ParameterBlock;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

//import jpl.mipl.wiio.io.plugins.VicarRenderedImage;

import javax.imageio.stream.*;

/***********************************************************************
 * Extremely simply image file format converter program.  Uses JAI imageread
 * and imagewrite operators to do the conversion.  No metadata is converted,
 * The bands to use are selected. Then the data type is converted to byte.
 * <p>
 * The four (mandatory) arguments are: input_filename, output_filename, type, bands
 * where type is one of the registered Image I/O writer plugin names.
 * Running the program with no arguments prints a list or available plugins.
 * <p>
 * If the output is a TIFF image, the tile size is set to 8-line strips.
 * Otherwise, the tile size defaults (generally copied from the input image).
 */



public class SimpleConvertBandSelect
{
    /** See class comments */
	
	static boolean gdebug = true;
	
	
		
		
	
	
// constructors
public SimpleConvertBandSelect() {
		System.out.println("SimpleConvertVIMS constuctor with no arguments");
	}

public SimpleConvertBandSelect(String argv[]) {
	boolean debug = gdebug;
	
	boolean bandSelect = true;
	
	int outputDataType = DataBuffer.TYPE_BYTE;
	//   s = "BYTE";
	boolean rescaleOnFormat = true;
	int bandList[] = {0,1,2};
	String value = argv[3];
		// split the value on [(,]) chars
		// no checking is currently done to see that bands selected are valid for 
		// the input image
		String s[] = value.split("[\\[\\(,\\)\\]]") ;
		
		// System.out.println("BANDS value="+value+"  length="+s.length);  
		int ii = 0;
		int bandCt = 0;
		
		// determine how many bands are in the input and create an array thast size
		// an array with extra items will kill the Operator
		System.out.println("count the bands --------------------");
		for (int j=0 ; j< s.length ; j++) {
			// System.out.println(j+">"+s[j]+"< "+s[j].length());
			// sometimes a zero length array member in s[] will exist, ignore it
			if (s[j].length() != 0 && ii < 3) {
				try {					
					bandList[ii] = Integer.parseInt(s[j]);
					}
				catch (NumberFormatException nfe) {
					System.out.println("BANDS NumberFormatException "+nfe);
					// bandList[ii] = 0;
					}
				bandCt++;
			}
		}
		bandList = new int[bandCt];
		
		for (int j=0 ; j< s.length ; j++) {
			// System.out.println(j+">"+s[j]+"< "+s[j].length());
			// sometimes a zero length array member in s[] will exist, ignore it
			if (s[j].length() != 0 && ii < 3) {
				try {					
					bandList[ii] = Integer.parseInt(s[j]);
				}
				catch (NumberFormatException nfe) {
					System.out.println("BANDS NumberFormatException "+nfe);
					bandList[ii] = 0;
				}
				ii++;
			}
		}
		
		ImageReader reader = null;
		ImageWriter writer = null;
		ImageInputStream iis = null;
		String readerFormat = "-";
		String readerClassName = "-";
		RenderedImage renderedImage = null;
		String fileName = argv[0];
		try {
            iis = ImageIO.createImageInputStream(new File(fileName));
        

            if (iis == null) {
                System.out.println("Unable to get a stream!");
                System.exit(1);// 1 is error return
            }

			
			
            	Iterator iter = ImageIO.getImageReaders(iis);
            
            	while (iter.hasNext()) {
                	reader = (ImageReader)iter.next();
                	if (debug) System.out.println("Using " +
                               reader.getClass().getName() +
                               " to read.");
                	readerClassName = reader.getClass().getName() ;
                	// get the format we are reading
                	readerFormat = reader.getFormatName();
                	break;
            	}
        	

            if (reader == null) {
                System.err.println("Unable to find a reader!");
                System.exit(1); // 1 is error return
            }
		} catch (IOException ioe) {
            System.out.println("I/O exception !");
            System.exit(1); // 1 is error return
        } 
        
        	reader.setInput(iis, true);
        
            

	// RenderedImage img = JAI.create("imageread", argv[0]);
		try {
			ImageReadParam param = reader.getDefaultReadParam();
			// add something to it
 			renderedImage = reader.readAsRenderedImage(0, param);
// 			if (renderedImage instanceof VicarRenderedImage ) {
// 				VicarRenderedImage vri = (VicarRenderedImage) renderedImage;
// 				// vri.setTileWidth(vri.getWidth());
// 			}
        } catch (IOException ioe) {
            System.out.println("I/O exception !");
            System.exit(1); // 1 is error return
        } finally {
        	
        }
        
	SampleModel sm = renderedImage.getSampleModel();
	RenderedImage bandSelectImg = null;
   
    int dataType = sm.getDataType();   
    int bands = sm.getNumBands();
	// band select
	if (bandSelect == true) {
		
		if (debug == true) {
			System.out.println("creating 3 band image  from "+bands+" bands, use BandSelect **");
			for (int i=0 ; i<bandList.length ; i++) {
				System.out.print(bandList[i]+",");
			}
			System.out.println("");
			
		}
		// int[] bandList = {0,1,2};
		// int[] bandList = {2,4,7};
		ParameterBlockJAI bandSelectPB = new ParameterBlockJAI("BandSelect");
		// bandSelectPB = new ParameterBlock();
		bandSelectPB.addSource(renderedImage);
		bandSelectPB.setParameter("bandIndices",bandList);
		// bandSelectPB.add(currentBandList);
		bandSelectImg = JAI.create("BandSelect", bandSelectPB);
		if (debug == true) {
			SampleModel smod = bandSelectImg.getSampleModel();
			int b = smod.getNumBands();
			System.out.println("bandSelectImg bands = "+b);
			}	
		
		}
	// reformat each to byte
	RenderedImage processedImage = processFormat(bandSelectImg, outputDataType, rescaleOnFormat);

	ParameterBlockJAI pb = new ParameterBlockJAI("imagewrite");
	pb.addSource(processedImage);
	pb.setParameter("output", argv[1]);
	pb.setParameter("format", argv[2]);
	if (argv[2].equalsIgnoreCase("tiff") ||
	    argv[2].equalsIgnoreCase("tif")) {

	    Dimension tilesize = new Dimension(processedImage.getWidth(), 8);
	    pb.setParameter("tilesize", tilesize);
	}
			
	JAI.create("imagewrite", pb);
	
	
  }


public static void main(String argv[])
{
 if (argv.length < 4) {
    System.out.println("Usage:");
    System.out.println("java SimpleConvert input output format bands");
    System.out.println("Bands should be specified as 2,3,4 ");
    System.out.println("no spaces between the band number. Bands start at 0.") ;
    System.out.println(" The band numbers are used as r,g,b to create a color image.");
    System.out.println(" A sungle band may be specified and will produce a single band grayscale image");
    System.out.println("where input and output are filenames and format is one of the following:");
    String[] formats = ImageIO.getWriterFormatNames();
    java.util.Arrays.sort(formats);
    for (int i=0; i < formats.length; i++)
	System.out.println("  " + formats[i]);
    System.out.println("Note that not all of the above can handle 16-bit data!");
    System.out.println("Output tile size for TIFF (only) defaults to 8 lines by the image width");
    System.exit(0);
 }

 System.out.println("create SimpleConvertBandSelect");
 SimpleConvertBandSelect convertOne = new  SimpleConvertBandSelect(argv) ;
 System.out.println("******************** after SimpleConvertBandSelect");
}

 // convert the input image to a different DataType
public RenderedImage processFormat(RenderedImage image, int newDataType, boolean rescaleOnFormat) {
        
        // DataBuffer.TYPE_BYTE
        RenderedImage sourceImage = image;
        boolean debug = gdebug;
        
        ComponentSampleModel sampleModel = (ComponentSampleModel) image.getSampleModel();
        int oldDataType = sampleModel.getDataType();
        int numbands = sampleModel.getNumBands();
        
        
        // check if (oldDataType == newDataType) return image;
        if (debug) {
        	
        	System.out.println("processFormat "+numbands+" bands   "+oldDataType+ " -> "+newDataType);
        	System.out.println("processFormat "+numbands+" bands   "+getDataTypeName(oldDataType)+ " -> "+getDataTypeName(newDataType));
        	System.out.println("rescaleOnFormat "+rescaleOnFormat);
        }
        
		   // make a new  SampleModel for the new image data type
		   // get all the stuff we need to know from the old sampleModel
		  int pStride =  sampleModel.getPixelStride();
		  int slStride = sampleModel.getScanlineStride();
		  int[] bandOffsets = sampleModel.getBandOffsets();
		  if (debug) System.out.println(" *** pStride="+pStride+"  slStride="+slStride+"  bandOffsets="+bandOffsets );
           //  int w = sampleModel.getWidth();
           // int h = sampleModel.getHeight();
         // ---------------------------------------------------------
         // rescale the pixel values of the image based on the image extrema
         RenderedImage temp = image;
        
     
     /* rescale should apply only if going from a bigger to smaller data type
      * turn it off if going to a wider data type
      * * if it is false here it means the user set it to false and is 
      * requesting NO rescale
      * The default is TRUE
      */   
      
    if (rescaleOnFormat == true) {
         	rescaleOnFormat =  checkRescaleOnFormat(oldDataType, newDataType);
         }
	  
    if (rescaleOnFormat) {
    	if (debug) System.out.println("processFormat rescaleOnFormat is TRUE - rescaling");
		ParameterBlock PB=new ParameterBlock();
		PB.addSource(temp).add(null).add(1).add(1);
		RenderedImage extrema=JAI.create("extrema",PB);

		// scale all pixels by:   v1= m * v0 +b    (assuming one band per pixel)
		double scale[][]=(double[][])extrema.getProperty("extrema");
		// double ceiling=Byte.MAX_VALUE*2; // treat as unsigned
		double ceiling = getMaxForDataType(newDataType) ;
	
	
	
		// double ceiling=Short.MAX_VALUE*2;
		double max=1,min=ceiling;
		for(int i=0;i<scale[0].length;i++){
	    	max=Math.max(max,scale[1][i]);
	    	min=Math.min(min,scale[0][i]);
		}
		if (debug) System.out.println("processFormat(1) extrema new ceiling="+ceiling+"  min="+min+"  max="+max);
		// round max up to the nearest power of 2. 
		// max=Math.pow(2.0,Math.round(Math.log(max)/Math.log(2)));
		// min=0;
	
	
		// this will be for BYTE output
		double constant[] = new double[]{1.0};
		double offset[] = new double[]{0.0};
	
	
		// dst[x][y][b] = src[x][y][b]*constant + offset;

		constant[0] = ceiling /(max-min);
		offset[0] = min * constant[0] * -1.0; // offset is added only for unsigned ??
		// offset[0] = min * -1.0; // offset is added only for unsigned ??
	
		if (debug) {
			System.out.println("processFormat(2) constant="+constant[0]+"  offset="+offset[0]);
	
	
			double min1 = (min * constant[0]) + offset[0];
			double max1 = (max * constant[0]) + offset[0];
			System.out.println("processFormat(3)  min="+min+"  min1="+min1+"  max="+max+"  max1="+max1);
		}
	
		PB=new ParameterBlock();
		// PB.addSource(temp).add(new double[]{ceiling/(max-min)}).add(new double[]{ceiling*min/(min-max)});
		PB.addSource(temp).add(constant).add(offset);
		temp=JAI.create("rescale",PB);
    } else {
    	if (debug) System.out.println("processFormat rescaleOnFormat is FALSE - NO rescaling !!!");
    }

	
    image = temp;    
        // ---------------------------------------------------------
        // rendering hint with a ImageLayout
        
        
        
        
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(image);
		pb.add(newDataType);
				
		// RenderedImage  formatedImage = JAI.create("format", pb, hints);
		RenderedImage  formatedImage = JAI.create("format", pb);
		
		return (formatedImage);    
    }
    
    /**
     * RescaleOnFormat should only be true if the output format is smaller 
     * than the input format.
     * 
     * @param inDataType
     * @param outDataType
     * @return true if rescale should continue, false if it should not
     */
	public boolean checkRescaleOnFormat(int inDataType, int outDataType) {
    	
    		boolean doRescale = false;
    		boolean debug = gdebug;
    		
    		if (debug) {
    			System.out.println("checkRescaleOnFormat in="+inDataType+" out="+outDataType);
    			System.out.println("checkRescaleOnFormat in="+getDataTypeName(inDataType)+ " out="+getDataTypeName(outDataType));
    		}
    		
			if (inDataType == DataBuffer.TYPE_BYTE) {				
			
				doRescale = false;  // no point to rescaling
				}
			else if (inDataType == DataBuffer.TYPE_SHORT) {
				// how to handle USHORT ?? 
				if (outDataType == DataBuffer.TYPE_BYTE ) {
					doRescale = true;
					}
				else {
					doRescale = false;
					}
				
				}	    
			else if (inDataType == DataBuffer.TYPE_USHORT) {	    	
				if (outDataType == DataBuffer.TYPE_BYTE ||
					outDataType == DataBuffer.TYPE_SHORT ) {
					doRescale = true;
					}
				else {
					doRescale = false;
					}
				}
			else if (inDataType == DataBuffer.TYPE_INT) {
				
				if (outDataType == DataBuffer.TYPE_BYTE ||
					outDataType == DataBuffer.TYPE_SHORT ||
					outDataType == DataBuffer.TYPE_USHORT) {
					doRescale = true;
					}
				else {
					doRescale = false;
					}
				}	    
			else if (inDataType == DataBuffer.TYPE_FLOAT) {
				if (outDataType == DataBuffer.TYPE_BYTE ||
					outDataType == DataBuffer.TYPE_SHORT ||
					outDataType == DataBuffer.TYPE_USHORT ||
					outDataType == DataBuffer.TYPE_INT) {
					doRescale = true;
					}
				else {
					doRescale = false;
					}
				}
			else if (inDataType == DataBuffer.TYPE_DOUBLE) {
				if (outDataType == DataBuffer.TYPE_DOUBLE ) {
					doRescale = false;
					}
				else {
					doRescale = true;
					}
				
				}
		if (debug) {
						System.out.println("checkRescaleOnFormat doRescale="+doRescale);
					}
    		
			return doRescale;
		 }
    /**
     * convenience for using format/rescale operators
     * @param dataType
     * @return double
     */
    public double getMinForDataType(int dataType) {
    	
    	double min = 0.0;
    	if (dataType == DataBuffer.TYPE_BYTE) {
    		// min = Byte.MIN_VALUE;
    		min = 0.0; //used as unsigned
	        }
	    else if (dataType == DataBuffer.TYPE_SHORT) {
	    	min = Short.MIN_VALUE;
	        }	    
	    else if (dataType == DataBuffer.TYPE_USHORT) {	    	
	    	min = 0.0;
	        }
	    else if (dataType == DataBuffer.TYPE_INT) {
	    	min = Integer.MIN_VALUE; // or 0.0 ?? // assume unsigned ???
	        }	    
	    else if (dataType == DataBuffer.TYPE_FLOAT) {
	    	min = Float.MIN_VALUE; 
	        }
	    else if (dataType == DataBuffer.TYPE_DOUBLE) {
	    	min = Double.MIN_VALUE; 
	        }
	    
	    return min;
	 }
	
	/**
     * convenience for using format/rescale operators
     * @param dataType
     * @return double
     */
    public double getMaxForDataType(int dataType) {
    	
    	double max = 0.0;
    	if (dataType == DataBuffer.TYPE_BYTE) {
    		max = Byte.MAX_VALUE * 2;//used as unsigned
	        }
	    else if (dataType == DataBuffer.TYPE_SHORT) {
	    	max = Short.MAX_VALUE;
	        }	    
	    else if (dataType == DataBuffer.TYPE_USHORT) {	    	
	    	max = Short.MAX_VALUE * 2;
	        }
	    else if (dataType == DataBuffer.TYPE_INT) {
	    	max = Integer.MAX_VALUE; // or 0.0 ?? // assume unsigned ???
	        }	    
	    else if (dataType == DataBuffer.TYPE_FLOAT) {
	    	max = Float.MAX_VALUE; 
	        }
	    else if (dataType == DataBuffer.TYPE_DOUBLE) {
	    	max = Double.MAX_VALUE; 
	        }
	    
	    return max;
	 }
    
    /**
     * convenience for using format/rescale operators
     * @param dataType
     * @return double
     */
    public String getDataTypeName(int dataType) {
    	
    	String name = "";
    	if (dataType == DataBuffer.TYPE_BYTE) {
    		name = "DataBuffer.TYPE_BYTE";
	        }
	    else if (dataType == DataBuffer.TYPE_SHORT) {
	    	name = "DataBuffer.TYPE_SHORT";
	        }	    
	    else if (dataType == DataBuffer.TYPE_USHORT) {	    	
	    	name = "DataBuffer.TYPE_USHORT";
	        }
	    else if (dataType == DataBuffer.TYPE_INT) {
	    	name = "DataBuffer.TYPE_INT";
	        }	    
	    else if (dataType == DataBuffer.TYPE_FLOAT) {
	    	name = "DataBuffer.TYPE_FLOAT";
	        }
	    else if (dataType == DataBuffer.TYPE_DOUBLE) {
	    	name = "DataBuffer.TYPE_DOUBLE"; 
	        }
	    
	    return name;
	 }

}

