package com.sap.spatialex;

public interface spatialExFeed {

    public static final int SUCCESS = 0;
    public static final int INVALID_BITSTREAM = -21;
    public static final int ERROR_READING_FIRST_PAGE = -22;
    public static final int ERROR_READING_INITIAL_HEADER_PACKET = -23;
    public static final int NOT_SAP_HEADER = -24;
    public static final int CORRUPT_SECONDARY_HEADER = -25;
    public static final int PREMATURE_END_OF_FILE = -26;

    public void decodeCallBack(short[] pcmdata, int amountToRead);
    public void stopCallBack();
    public void startCallBack();
    
}
