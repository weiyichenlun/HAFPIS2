package hbie2.HAFPIS2.Utils;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/3/28
 * 最后修改时间:2018/3/28
 */
public class StringUtil {
    public static String addQuotes(String s) {
        return "\'" + s + "\'";
    }

    public static String addQuotes(Integer i) {
        return "\'" + i + "\'";
    }
}
