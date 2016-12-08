package jpl.mipl.wiio.store.read;

import java.util.LinkedHashMap;
import java.util.Map;

import jpl.mipl.mars.pig.PigCameraModel;
import jpl.mipl.mars.pig.PigConstants;
import jpl.mipl.mars.pig.PigCoreCAHV;
import jpl.mipl.mars.pig.PigCoreCAHVOR;
import jpl.mipl.mars.pig.PigCoreCAHVORE;
import jpl.mipl.mars.pig.PigImageCoordinate;
import jpl.mipl.mars.pig.PigLookVector;
import jpl.mipl.mars.pig.PigPoint;
import jpl.mipl.mars.pig.PigVector;

/**
 * Examines an instance of PigCameraModel and returns a
 * set of attribute maps pertaining to that model.  
 * Currently supported are: 
 * 1) camera model parameters, which detail the type and 
 * settings of the camera;
 * 2) pointing parameters, which include camera position
 * and orientation.
 *
 * @author Nicholas Toole (Nicholas.T.Toole@jpl.nasa.gov)
 * @version $Id: $
 *
 */
public class CameraModelInfoExtractor
{

    protected PigCameraModel cmod;
    
    public boolean VALUES_STRINGS_DEFAULT = true;
    
    protected boolean valuesAsStrings = VALUES_STRINGS_DEFAULT;
    
    //---------------------------------------------------------------------
    
    /**
     * Constructor
     * @param cmod Camera model instance
     */
    public CameraModelInfoExtractor(PigCameraModel cmod)
    {
        this.cmod = cmod;
        init();
    }
    
    
    //---------------------------------------------------------------------
    
    protected void init()
    {
        this.valuesAsStrings = VALUES_STRINGS_DEFAULT;
//        this.map = new LinkedHashMap();
    }

    //---------------------------------------------------------------------
    
    /**
     * Sets values returned by this class to be of type String or native
     * type returned from camera model
     * @param flag True if values should be strings, false otherwise
     */
    public void setValuesAsStrings(boolean flag)
    {
        this.valuesAsStrings = flag;
    }
    
    //---------------------------------------------------------------------
    
    /**
     * Returns true if values should be strings, false otherwise
     * @return Value as string state
     */
    public boolean isValuesAsStringsEnabled()
    {
        return this.valuesAsStrings;
    }
    
    //---------------------------------------------------------------------
    
    /**
     * Returns the camera model type, reference frame and type-specific 
     * parameters.  Currently, only supported camera model types are:
     * CAHV, CAHVOR, CAHVORE.
     * @return Map of camera model metadata
     */
    public Map<String,Object> getRootParameters()
    {        
        LinkedHashMap<String,Object> map = new LinkedHashMap<String,Object>();
        if (cmod == null)
            return map;
        
        final String cmodType = this.getCameraModelType();
        insertValue(map, CmodConstants.KEY_MODEL_TYPE,  cmodType);
        
        return map;
        
    }
    //---------------------------------------------------------------------
    
    /**
     * Returns the camera model type, reference frame and type-specific 
     * parameters.  Currently, only supported camera model types are:
     * CAHV, CAHVOR, CAHVORE.
     * @return Map of camera model metadata
     */
    public Map<String,Object> getCameraParameters()
    {        
        LinkedHashMap<String,Object> map = new LinkedHashMap<String,Object>();
        if (cmod == null)
            return map;
        
        
        //----------------------
        //common attributes
        
        //reference frame
        String refFrame = cmod.getRefFrame();
        if (refFrame != null)
            insertValue(map, CmodConstants.KEY_REFERENCE_FRAME, refFrame);
        
        //pixel angles
        double linePixAngle = cmod.getPixelAngle(PigConstants.TYPE_LINE);
        double sampPixAngle = cmod.getPixelAngle(PigConstants.TYPE_SAMPLE);
        double[] pixelAngles = new double[] {linePixAngle, sampPixAngle};
        insertValue(map, CmodConstants.KEY_PIXEL_ANGLE, pixelAngles);
        
        //let folks know we giving them "line,sample"
        insertValue(map, CmodConstants.KEY_IMAGE_COORD_ORDER, 
                         CmodConstants.VAL_IMAGE_COORD_ORDER);
        
        //----------------------
        
        //by default, we do not know about the camera type
        insertValue(map, CmodConstants.KEY_MODEL_TYPE, 
                         CmodConstants.MODEL_TYPE_UNKNOWN);
        
        
        //specialized stuff
        if (cmod instanceof PigCoreCAHV)
        {
            PigCoreCAHV cahv = (PigCoreCAHV) cmod;
            
            insertValue(map, CmodConstants.KEY_MODEL_TYPE, 
                             CmodConstants.MODEL_TYPE_CAHV);
            
            PigVector cVec = cahv.getC();
            PigVector aVec = cahv.getA();
            PigVector hVec = cahv.getH();
            PigVector vVec = cahv.getV();
            
            double[] c = pigVecToDArray(cVec);
            double[] a = pigVecToDArray(aVec);
            double[] h = pigVecToDArray(hVec);
            double[] v = pigVecToDArray(vVec);
            
            if (c != null && a != null && h != null && v != null)
            {
                insertValue(map, CmodConstants.KEY_C, c);
                insertValue(map, CmodConstants.KEY_A, a);
                insertValue(map, CmodConstants.KEY_H, h);
                insertValue(map, CmodConstants.KEY_V, v);  
            }   
        }
        if (cmod instanceof PigCoreCAHVOR)
        {
            PigCoreCAHVOR cahvor = (PigCoreCAHVOR) cmod;
            
            insertValue(map, CmodConstants.KEY_MODEL_TYPE, 
                             CmodConstants.MODEL_TYPE_CAHVOR);
            
            PigVector oVec = cahvor.getO();
            PigVector rVec = cahvor.getR();
            
            double[] o = pigVecToDArray(oVec);
            double[] r = pigVecToDArray(rVec);
            
            if (o != null && r != null)
            { 
                insertValue(map, CmodConstants.KEY_O, o);
                insertValue(map, CmodConstants.KEY_R, r);
            }
            
        }
        if (cmod instanceof PigCoreCAHVORE)
        {
            PigCoreCAHVORE cahvore = (PigCoreCAHVORE) cmod;
            
            insertValue(map, CmodConstants.KEY_MODEL_TYPE, 
                             CmodConstants.MODEL_TYPE_CAHVORE);
            
            PigVector eVec = cahvore.getE();
            int       mTyp = cahvore.getPupilType();
            double    mPrm = cahvore.getPupilParam();
            
            String pupil = translatePupilType(mTyp);
            
            double[] e = pigVecToDArray(eVec);
            
            if (e != null)
            {
                insertValue(map, CmodConstants.KEY_E, e);
                insertValue(map, CmodConstants.KEY_M, pupil);
                insertValue(map, CmodConstants.KEY_P, mPrm);
            }
        }   
     
        return map;
    }

    //---------------------------------------------------------------------
    
    protected String translatePupilType(final int pupilType)
    {
        String pupilStr = null;
        
        if (pupilType == PigConstants.CAHVORE_PUPILTYPE_PERSPECTIVE)
        {
            pupilStr = CmodConstants.PUPIL_TYPE_PERSPECTIVE;
        }
        else if (pupilType == PigConstants.CAHVORE_PUPILTYPE_FISHEYE)
        {
            pupilStr = CmodConstants.PUPIL_TYPE_FISHEYE;
        }
        else if (pupilType == PigConstants.CAHVORE_PUPILTYPE_GENERAL)
        {
            pupilStr = CmodConstants.PUPIL_TYPE_GENERAL;
        }
        return pupilStr;
        
    }
    //---------------------------------------------------------------------
    
    protected String getCameraModelType()
    {
        //specialized stuff
        if (cmod == null)
        {
            return CmodConstants.MODEL_TYPE_UNKNOWN;
        }
        else if (cmod instanceof PigCoreCAHVORE)
        {
            return CmodConstants.MODEL_TYPE_CAHVORE;
        }
        else if (cmod instanceof PigCoreCAHVOR)
        {
            return CmodConstants.MODEL_TYPE_CAHVOR;
        }
        else if (cmod instanceof PigCoreCAHV)
        {
            return PigConstants.MODELTYPE_CAHV;
        }
        return CmodConstants.MODEL_TYPE_UNKNOWN;
    }
        
    
    //---------------------------------------------------------------------
    
    public Map<String,Object> getPointingParameters()
    {        
        LinkedHashMap<String,Object> map = new LinkedHashMap<String,Object>();
        if (cmod == null)
            return map;
        
        
        
        //let folks know we giving them "line,sample"
        insertValue(map, CmodConstants.KEY_IMAGE_COORD_ORDER, 
                         CmodConstants.VAL_IMAGE_COORD_ORDER);
        
        
        //get the image coordinates of where camera is looking
        PigImageCoordinate imgCrd = cmod.getCameraCenter();
        if (imgCrd == null)
            return map;
        double[] centerCoords = new double[] {imgCrd.getLine(),imgCrd.getSample()};            
        insertValue(map, CmodConstants.KEY_CAMERA_CENTER_PIXEL, centerCoords);
        
        
        //for that imgCoord, find camera position and orientation
        PigLookVector lookVecs = cmod.LStoLookVector(imgCrd);
        
        if (lookVecs != null)
        {
            PigPoint  originPnt = lookVecs.getOrigin();
            PigVector lookAtVec = lookVecs.getLookAt();
            
            double[] origin = pigVecToDArray(originPnt);
            double[] lookAt = pigVecToDArray(lookAtVec);
            double[] upVect = CmodConstants.DEFAULT_UP_VECTOR;
            
            if (origin != null && lookAt != null)
            {    
                insertValue(map, CmodConstants.KEY_CAMERA_POSITION, origin);
                insertValue(map, CmodConstants.KEY_CAMERA_LOOKAT,   lookAt);
                insertValue(map, CmodConstants.KEY_CAMERA_UPVECTOR, upVect);
            }
            
        }
        
       
        return map;
    }
    
    //---------------------------------------------------------------------
    
    protected static double[] pigVecToDArray(final PigVector pigVec)
    {
        if (pigVec == null)
            return null;
        
        double[] dubArr = new double[] {pigVec.getX(),pigVec.getY(),pigVec.getZ()};
        return dubArr;
    }
    
    //---------------------------------------------------------------------
    
    protected static String pigVecToDArrayStr(final PigVector pigVec)
    {
        if (pigVec == null)
            return null;
        
        String str = "{"+pigVec.getX()+","+pigVec.getY()+","+pigVec.getZ()+"}";
        return str;
    }
    
    //---------------------------------------------------------------------
    
    protected static String dArrayToStr(final double[] dArray)
    {
        if (dArray == null)
            return null;
        
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        for (int i = 0; i < dArray.length; ++i)
        {
            buffer.append(dArray[i]);
            if (i + 1 < dArray.length)
            {
                buffer.append(",");
            }
        }
        buffer.append("}");
        return buffer.toString();
    }
    
    //---------------------------------------------------------------------
    
    protected static String[] dArrayToStrArray(final double[] dArray)
    {
        if (dArray == null)
            return null;        
        String[] strArray = new String[dArray.length];                
        for (int i = 0; i < dArray.length; ++i)
            strArray[i] = dArray[i] + "";
        return strArray;
    }
    
    //---------------------------------------------------------------------
    
    protected static String[] iArrayToStrArray(final int[] iArray)
    {
        if (iArray == null)
            return null;        
        String[] strArray = new String[iArray.length];                
        for (int i = 0; i < iArray.length; ++i)
            strArray[i] = iArray[i] + "";
        return strArray;
    }
    
    //---------------------------------------------------------------------
    
    protected static String sArrayToStr(final String[] sArray)
    {
        if (sArray == null)
            return null;
        
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        final int subArrayLen = sArray.length - 1;
        for (int i = 0; i < subArrayLen; ++i)
        {
            buffer.append(sArray[i]).append(",");            
        }
        if (subArrayLen > 0)
            buffer.append(sArray[subArrayLen]);
        buffer.append("}");
        return buffer.toString();
    }
    
    //---------------------------------------------------------------------
    
    //---------------------------------------------------------------------
    
    protected void insertValue(Map map, String key, Object value)
    {
        if (this.valuesAsStrings)
        {
            String valStr = value.toString();
            
            //so far we only deal with double arrays, strings, and ints
            if (value instanceof double[])
            {
                String[] strArr = dArrayToStrArray((double[])value);
                valStr = sArrayToStr(strArr);
            }
            else if (value instanceof String[])
            {
               valStr = sArrayToStr((String[])value);
            }    
            map.put(key, valStr);
        }
        else
        {
            map.put(key, value);
        }
    }
    
    //---------------------------------------------------------------------
    
    //---------------------------------------------------------------------
 
}

