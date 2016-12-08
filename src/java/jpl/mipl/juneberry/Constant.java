package jpl.mipl.juneberry;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

/**
 * @author Xing
 */
public class Constant {
    public static final String APP_NAME = "juneberry";
    public static final String APP_VERSION = "0.11.2"; //nttoole (2016.09.08)

    public static final String SIZE = "size";

    //public static final String TYPE = "type";
    //public static final String DIMS = "dimensions";
    //public static final String SHAPE = "shape";

    public static final String READ_CACHE = APP_NAME+"-"+APP_VERSION+".read.cache";
    public static final String OUTPUT_CACHE = APP_NAME+"-"+APP_VERSION+".output.cache";

    public static final String INIT_PARAM_W10N_OF_FS_DIR_NOT_BY_CGI_SERVLET = "w10n_of_fs_dir_not_by_cgi_servlet";
    public static final String SYSTEM_PROPERTY_W10N_OF_FS_DIR_NOT_BY_CGI_SERVLET = "juneberry.w10n_of_fs_dir_not_by_cgi_servlet";

    public static final String INIT_PARAM_PATH_PREFIX = "path_prefix";
    public static final String SYSTEM_PROPERTY_PATH_PREFIX = "juneberry.path_prefix";

    public static final String INIT_PARAM_FS_DIR_STORE_ROOT = "fs_dir_store_root";
    public static final String SYSTEM_PROPERTY_FS_DIR_STORE_ROOT = "juneberry.fs_dir_store_root";
    public static final String FS_DIR_STORE_ROOT = "/var/www/html";

    public static final String INIT_PARAM_ENTRY_LIST = "entry_list";
    public static final String SYSTEM_PROPERTY_ENTRY_LIST = "juneberry.entry_list";
}
