package hbie2.nist.format;

import com.sun.jna.FunctionMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallFunctionMapper;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

/**
 * Created by pms on 2017/3/13.
 */
public interface CxbioLibrary extends Library {
    public static final CxbioLibrary INSTANCE = (CxbioLibrary) Native.loadLibrary("cxbio", CxbioLibrary.class, new HashMap<String,FunctionMapper>(){{
        put(Library.OPTION_FUNCTION_MAPPER, new StdCallFunctionMapper());
    }});
    public static final int CXBIO_FORMAT_JPGL = 3;
    public static final int CXBIO_FORMAT_WSQ = 1;
    public static final int CXBIO_FORMAT_JP2 = 4;
    public static final int CXBIO_ENCODING_ERROR = 4;
    public static final int CXBIO_FORMAT_BMP = 7;
    public static final int CXBIO_ERROR_SUCCESS = 0;
    public static final int CXBIO_FORMAT_UNKNOWN = 0;
    public static final int CXBIO_IMAGE_FORMAT_ERROR = 7;
    public static final int CXBIO_INBUFF_ERROR = 2;
    public static final int CXBIO_FORMAT_PNG = 6;
    public static final int CXBIO_FILE_OPEN_ERROR = 8;
    public static final int CXBIO_FORMAT_JPG = 2;
    public static final int CXBIO_COMPRESSIONRATE_ERROR = 6;
    public static final int CXBIO_FORMAT_JP2L = 5;
    public static final int CXBIO_TYPE_ERROR = 3;
    @Deprecated
    int CxbioEncode(EncInParam inparam, PointerByReference outBuf, IntByReference outLength);
    int CxbioEncode(EncInParam inparam, PointerByReference outBuf, IntBuffer outLength);
    @Deprecated
    int CxbioGetImageData(Pointer inBuf, int inLength, int inType, DecOutParam outResult);
    int CxbioGetImageData(ByteBuffer inBuf, int inLength, int inType, DecOutParam outResult);
    void CxbioFree(Pointer ptr);
}
