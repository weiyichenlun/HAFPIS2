package hbie2.HAFPIS2.Utils;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class CONSTANTS {
    public static final int NCORES = Runtime.getRuntime().availableProcessors();

    public static final boolean DELETE = false;
    public static final boolean UPDATE = true;

    // HAFPIS_SRCH_TASK Status
    public static final int URGENT_STATUS = 99;
    public static final int WAIT_STATUS = 3;
    public static final int PROCESSING_STATUS = 4;
    public static final int FINISH_STATUS = 5;
    public static final int FINISH_NOMATCH_STATUS = 6;
    public static final int ERROR_STATUS = -1;

    //HAFPIS_SRCH_TASK DATATYPE
    public static final int SRCH_DATATYPE_TP = 1;
    public static final int SRCH_DATATYPE_PP = 2;
    public static final int SRCH_DATATYPE_LPP = 4;
    public static final int SRCH_DATATYPE_PLP = 5;
    public static final int SRCH_DATATYPE_FACE = 6;
    public static final int SRCH_DATATYPE_IRIS = 7;

    //HAFPIS_RECORD_STATUS
    public static final int RECORD_DATATYPE_TP = 1;
    public static final int RECORD_DATATYPE_PP = 2;
    public static final int RECORD_DATATYPE_LPP = 4;
    public static final int RECORD_DATATYPE_PLP = 5;
    public static final int RECORD_DATATYPE_FACE = 6;
    public static final int RECORD_DATATYPE_IRIS = 7;

    //HAFPIS_SRCH_TASK TASKTYPE
    public static final int SRCH_TASKTYPE_TT = 1;
    public static final int SRCH_TASKTYPE_TL = 2;
    public static final int SRCH_TASKTYPE_LT = 3;
    public static final int SRCH_TASKTYPE_LL = 4;
    public static final int SRCH_TASKTYPE_1TOF = 8;

    // HAFPIS_DBOP_TASK DATATYPE
    public static final int DBOP_TPP = 3;
    public static final int DBOP_LPP = 4;
    public static final int DBOP_PLP = 5;

    // HAFPIS_DBOP_TASK TASKTYPE
    public static final int DBOP_INSERT = 5;
    public static final int DBOP_DELETE = 6;
    public static final int DBOP_UPDATE = 7;

    // HAFPIS_SRCH_TASK ArrayBlockingQueue size limit
    public static final int FPTT_LIMIT = 20;
    public static final int FPTL_LIMIT = 20;
    public static final int FPLT_LIMIT = 20;
    public static final int FPLL_LIMIT = 20;

    public static final int PPTT_LIMIT = 20;
    public static final int PPTL_LIMIT = 20;
    public static final int PPLT_LIMIT = 20;
    public static final int PPLL_LIMIT = 20;

    public static final int FACETT_LIMIT = 20;

    public static final int IRISTT_LIMIT = 20;

    public static final int FPTT_1TOF_LIMIT = 10;
    public static final int PPTT_1TOF_LIMIT = 10;
    public static final int FPLL_1TOF_LIMIT = 10;
    public static final int PPLL_1TOF_LIMIT = 10;
    public static final int FACE_1TOF_LIMIT = 10;
    public static final int IRIS_1TOF_LIMIT = 10;

    public static final int DBOP_TPP_LIMIT = 20;
    public static final int DBOP_LPP_LIMIT = 20;
    public static final int DBOP_PLP_LIMIT = 20;

    public static final int[] srchOrder = new int[]{0, 4, 5, 9};
    public static final int[] feaOrder = new int[]{0, 2, 1, 3};



    public static final int FpFeatureSize = 3072;
    public static final int[] PalmFeatureSize = new int[]{12288, 12288, 8192, 8192};
    public static final int FacefeatureSize = 1580;
    public static final int IrisFeatureSize = 2400;
    public static final int MAXCANDS = 100;

    public static final int SLEEP_TIME = 10;

    public static int ppPos2Ora(int position) {
        int a = 0;
        switch (position) {
            case 0:
                a = 1;
                break;
            case 1:
                a = 6;
                break;
            case 2:
                a = 5;
                break;
            case 3:
                a = 10;
                break;
        }
        return a;
    }
}
