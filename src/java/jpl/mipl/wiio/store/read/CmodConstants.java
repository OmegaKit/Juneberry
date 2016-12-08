package jpl.mipl.wiio.store.read;

/**
* Set of constants used in the cmod extraction and metadata conversion
*/
public interface CmodConstants
{
    public static final String KEY_CMOD              = "cmod";
    
    public static final String KEY_CAMERA_SETTINGS   = "camera_settings";
    public static final String KEY_POINTING_SETTINGS = "pointing_settings";
    
    //camera model type
    public static final String KEY_MODEL_TYPE      = "model_type";
    
    public static final String MODEL_TYPE_UNKNOWN  = "unknown";
    public static final String MODEL_TYPE_CAHV     = "cahv";
    public static final String MODEL_TYPE_CAHVOR   = "cahvor";
    public static final String MODEL_TYPE_CAHVORE  = "cahvore";
    
    public static final String PUPIL_TYPE_PERSPECTIVE = "perspective";
    public static final String PUPIL_TYPE_FISHEYE     = "fisheye";
    public static final String PUPIL_TYPE_GENERAL     = "general";
    
    // {  model_type = "cahv",  parameters = { c = (x,y,z), a = (... } }
    // {  position = (x,y,z), lookAt = (), fov = 45.0, frame = }
    
    //camera position
    public static final String KEY_CAMERA_POSITION  = "position";
    
    //look at vector (looks at the center of the image)
    public static final String KEY_CAMERA_LOOKAT    = "look_at";
    
    //upvector, fixed to a specific value?
    public static final String KEY_CAMERA_UPVECTOR  = "up_vector";
    
    //field of view
    public static final String KEY_CAMERA_FOV       = "fov";
    
    //reference frame
    public static final String KEY_REFERENCE_FRAME  = "reference_frame";
    
    //camera center points to pixel {line,sample}
    public static final String KEY_CAMERA_CENTER_PIXEL  = "center_pixel";
    
    //pixel angle {line,sample}
    public static final String KEY_PIXEL_ANGLE  = "pixel_angle";
    
    
    //pixel angle {line,sample}
    public static final String   VALUE_LINE             = "line";
    public static final String   VALUE_SAMPLE           = "sample";
    public static final String   KEY_IMAGE_COORD_ORDER  = "image_coord_order";
    public static final String[] VAL_IMAGE_COORD_ORDER  = {VALUE_LINE, VALUE_SAMPLE};
    
    public static final String KEY_C     = "c";
    public static final String KEY_A     = "a";
    public static final String KEY_H     = "h";
    public static final String KEY_V     = "v";
    public static final String KEY_O     = "o";
    public static final String KEY_R     = "r";
    public static final String KEY_E     = "e";
    public static final String KEY_M     = "m";
    public static final String KEY_P     = "p";
    
    
    public static final double[] DEFAULT_UP_VECTOR = new double[] { 0.0, 0.0, -1.0};
    
}
