package hbie2.nist.library;

/**
 * Created by pms on 2017/3/1.
 */
public class NistMntData  {
    public int idc;
    public int imp;
    public int dpi;
    public byte[] surSysName = new byte[64];
    /** A|U|E|M */
    public byte mntExMethod;
    /**
     * 0-36;<br>
     */
    public int[] nPossblyPos = new int[10];
    /**
     * A|W|R|L|U<br>
     */
    public byte[] nPattern = new byte[2];
    public int nNumOfCore;
    public NistCore coreList;
    public int nNumOfDelta;
    public NistDelta deltaList;
    public int bIsHaveRidge;
    public int nNumOfMinutia;
    public NistMinutia minutiaList;
    public NistMntData() {
        super();
    }


}
