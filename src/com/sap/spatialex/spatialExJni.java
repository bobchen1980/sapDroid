package com.sap.spatialex;

public class spatialExJni {
	public static native int SetFileSource(String filename);
	public static native int StartDecoding(spatialExFeed decodeFeed);
	public static native int StopDecoding();
	public static native int getCurPosition();
	public static native int SetSpatialEx(int param);
	public static native String  GetVersion();

    static {
    	try {
    		System.loadLibrary("avutil-52");
            System.loadLibrary("avcodec-55");
            System.loadLibrary("avformat-55");
            System.loadLibrary("swresample-0");
            System.loadLibrary("spatializer");
            System.loadLibrary("spatialexjni");
    	} catch (UnsatisfiedLinkError ule) {
    		System.err.println("JNI: Could not load library");
    		ule.printStackTrace(System.out);
    	} 
    } 
}
