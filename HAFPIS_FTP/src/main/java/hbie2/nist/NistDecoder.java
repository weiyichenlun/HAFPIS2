package hbie2.nist;

import hbie2.nist.nativesdk.NistPackDecode;
import hbie2.nist.nativesdk.NistPackDecodeImp;
import hbie2.nist.nistType.NistData;
import hbie2.nist.nistType.NistImg;
import hbie2.nist.nistType.RawNistData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/10
 * 最后修改时间:2018/4/10
 */
public class NistDecoder {
    private static Logger log = LoggerFactory.getLogger(NistDecoder.class);
    private static NistPackDecode nistPackDecode = new NistPackDecodeImp();


    public static Map<Integer, List<NistImg>> decode(String filePath) {
        Map<Integer, List<NistImg>> result = new HashMap<>();
        RawNistData rawNistData = nistPackDecode.DecodeInit(filePath);
        if (rawNistData == null) {
            log.error("Error in decode init. file: {}", filePath);
        }
        NistData nistdata = nistPackDecode.DecodeNistData(rawNistData);
        log.debug("Transno is {}", nistPackDecode.GetTransNo(nistdata));
        List<NistImg> nistImgs = nistdata.GetImgList();
        for (NistImg nistImg : nistImgs) {
            int type = nistImg.type;
            if (result.get(type) == null) {
                List<NistImg> temp = new ArrayList<>();
                temp.add(nistImg);
                result.put(type, temp);
            } else {
                List<NistImg> temp = result.get(type);
                temp.add(nistImg);
                result.put(type, temp);
            }
//            SdkUtils.writeImageFiles(nistImg);
        }
        return result;
    }

}
