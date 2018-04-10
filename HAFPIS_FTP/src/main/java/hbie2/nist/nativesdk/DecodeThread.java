package hbie2.nist.nativesdk;

import hbie2.nist.nistType.NistData;
import hbie2.nist.nistType.NistImg;
import hbie2.nist.nistType.NistTxt;
import hbie2.nist.nistType.RawNistData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by pms on 2017/4/5.
 */
public class DecodeThread extends Thread implements SdkHandler {

    public static Logger log = LoggerFactory.getLogger(DecodeThread.class);
    public List<NistImg> imageDatas;
    public List<NistTxt> txtDatas;
    public NistData nistData;
    public RawNistData rawNistData = new RawNistData();
    public String transNo;

    public NistPackDecode decode = new NistPackDecodeImp();
    public String filePath;

    public DecodeThread(String filePath) {
        this.filePath = filePath;
    }

    public void run() {
        rawNistData = decode.DecodeInit(filePath);
        if (rawNistData == null) {
            error(SdkErrorCode.DECODE_INIT_FAILED, "decode init failed");
        }
        nistData = decode.DecodeNistData(rawNistData);
        transNo = decode.GetTransNo(nistData);
        log.debug("作业号：{}", transNo);

        txtDatas = nistData.GetTxtList();
        if (txtDatas.size() != nistData.numOfTxt) {
            error(SdkErrorCode.DECODE_TXT_NUMBER_ERROR, "文本数量不一致");
        }
        SdkUtils.writeTxtFiles(txtDatas, "t_info");

        imageDatas = nistData.GetImgList();
        if (imageDatas.size() != nistData.numOfImg) {
            error(SdkErrorCode.DECODE_IMG_NUMBER_ERROR, "图像数量不一致");
        }
        for (NistImg img : imageDatas) {
            if (img.type != 8) {
                SdkUtils.writeImageFiles(img);
            }
            log.debug("image type  {} {}", img.type, img.idc);
        }
        log.debug(" image size {} ", nistData.numOfImg);
        log.debug(" txt size  {}", nistData.numOfTxt);

    }


    @Override
    public void error(SdkErrorCode code, String msg) {
        log.debug(code + msg);
    }
}
