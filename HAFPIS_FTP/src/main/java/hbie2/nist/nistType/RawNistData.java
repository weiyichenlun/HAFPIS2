package hbie2.nist.nistType;

/**
 * Created by pms on 2017/3/27.
 */
public class RawNistData {

    public int type;
    public int idc;
    public byte[] data;
    public int dataSize;

    public RawNistData() {
        super();
    }


    public RawNistData(byte[] data, int dataSize) {
        this.data = data;
        this.dataSize = dataSize;
    }

    public RawNistData(byte[] data, int dataSize, int type, int idc) {
        this.data = data;
        this.dataSize = dataSize;
        this.idc = idc;
        this.type = type;
    }
}
