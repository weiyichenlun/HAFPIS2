package hbie2.nist.nativesdk;

import hbie2.nist.nistType.NistData;
import hbie2.nist.nistType.NistImg;
import hbie2.nist.nistType.NistTxt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by pms on 2017/3/27.
 */
public class EncodeThread extends Thread implements SdkHandler {
    public static Logger log = LoggerFactory.getLogger(EncodeThread.class);
    public List<NistImg> imageDatas;
    public List<NistTxt> nistTxtDatas;
    public String nistFileName;
    public String TransNo;

    public RawImageEncode encode = new RawImageEncodeImp();


    public EncodeThread(String TransNo, List<NistImg> imageDatas, List<NistTxt> nistTxtDatas, String nistFileName) {
        this.TransNo = TransNo;
        this.imageDatas = imageDatas;
        this.nistTxtDatas = nistTxtDatas;
        this.nistFileName = nistFileName;
    }

    @Override
    public void run() {
        NistData nistData = encode.Nist_Init();
        /*初始化*/
        if (nistData == null) {
            error(SdkErrorCode.ENCODE_INIT_FAILED, "encode init failed");
            return;
        }
        boolean ret = false;

        ret = encode.AddTransNo(nistData, TransNo);
        /*添加字段文本信息*/
        if (nistData != null && nistTxtDatas != null) {
            for (NistTxt data : nistTxtDatas) {
                ret = encode.AddTxtData(nistData, data.fieldType, data.fieldIdc, data.fieldNum, data.value, data.valueLen);
                if (nistData == null || !ret) {
                    error(SdkErrorCode.DECODE_ADD_TXT_FAILED, "add txt failed");
                }
            }
        }
        if (nistData != null && imageDatas != null) {
         /*添加图像*/
            for (NistImg imageData : imageDatas) {
                ret = encode.AddImgData(nistData, imageData.type,
                        imageData.idc, imageData.imp, imageData.pos,
                        imageData.resX, imageData.resY,
                        imageData.cmsCode, imageData.width, imageData.height,
                        imageData.imgData, imageData.imgDataLen, null, 0);
                if (nistData == null || !ret) {
                    error(SdkErrorCode.DECODE_ADD_IMAGE_FAILED, "add image failed");
                }
            }
        }
        byte[] nist = encode.EncodeNistData(nistData);
        FileOutputStream nistFile = null;
        try {
            nistFile = new FileOutputStream(new File(nistFileName));
            log.debug("nistfile length:{}", nist.length);
            nistFile.write(nist);
            nistFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void error(SdkErrorCode code, String msg) {

    }
}
