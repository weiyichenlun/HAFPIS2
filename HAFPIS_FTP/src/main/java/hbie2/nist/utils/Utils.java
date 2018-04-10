package hbie2.nist.utils;

import java.nio.ByteBuffer;

/**
 * Created by pms on 2017/3/7.
 */
public class Utils {

    public static String getErrorMsg(ByteBuffer errMsg) {
            byte[] errdata = new byte[2048];
            errMsg.get(errdata);
            return new String(errdata);
    }
}
