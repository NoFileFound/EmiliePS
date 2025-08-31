package org.kcp.erasure.fec;

public class Fec {
    public static int fecHeaderSize = 6;
    public static int fecDataSize = 2;
    public static int fecHeaderSizePlus2 = fecHeaderSize + fecDataSize;
    public static int typeData = 0xf1;
    public static int typeParity = 0xf2;
}