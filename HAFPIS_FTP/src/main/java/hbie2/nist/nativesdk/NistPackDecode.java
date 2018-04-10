package hbie2.nist.nativesdk;

import hbie2.nist.nistType.NistData;
import hbie2.nist.nistType.RawNistData;

/**
 * Created by pms on 2017/3/13.
 */
public interface NistPackDecode {
    public RawNistData DecodeInit(String file);

    public NistData DecodeNistData(RawNistData rawNistData);

    public String GetTransNo(NistData nistData);

}
