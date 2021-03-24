package com.hisense.gateway.library.utils;

import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.constant.GatewayConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.constant.BaseConstants.TAG;

@Slf4j
public class CommonUtil {
    public static String escape(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        str = str.replaceAll("%", GatewayConstants.ESCAPESTR + "%")
                .replaceAll("_", GatewayConstants.ESCAPESTR + "_");
        return str;
    }

    public static String formatJson(String json) {
        return json.replaceAll("\\\"", "\"");
    }

    // guilai.ming 2020/09/10

    /**
     * 列表成员按逗号分隔,组成字符串
     */
    public static <T> String encodeListWithSplit(List<T> list, String split) {
        if (MiscUtil.isEmpty(list)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (T item : list) {
            sb.append(item).append(split);
        }
        return MiscUtil.removeSuffixComma(sb.toString());
    }

    /**
     * 从字符串中按逗号分隔, 提出列表成员
     */
    public static <T> List<T> decodeListWithSplit(String items, String split, ValueGenerator<T> generator) {
        String[] strIds;
        if (MiscUtil.isEmpty(items) || MiscUtil.isEmpty(strIds = MiscUtil.splitItems(items, split))) {
            return null;
        }

        return Arrays.stream(strIds).map(generator::valueOf).collect(Collectors.toList());
    }

    public interface ValueGenerator<T> {
        T valueOf(String valueStr);
    }

    public static <T> String encodeListWithComma(List<T> list) {
        return encodeListWithSplit(list, ",");
    }

    public static <T> List<T> decodeListWithComma(String items, ValueGenerator<T> generator) {
        return decodeListWithSplit(items, ",", generator);
    }

    public static String encodeStrListWithComma(List<String> list) {
        return encodeListWithComma(list);
    }

    public static List<String> decodeStrListWithComma(String items) {
        return decodeListWithComma(items, String::valueOf);
    }

    public static String encodeIntListWithComma(List<Integer> list) {
        return encodeListWithComma(list);
    }

    public static List<Integer> decodeIntListWithComma(String items) {
        return decodeListWithComma(items, Integer::valueOf);
    }

    public static String encodeStrListWithUnderline(List<String> list) {
        return encodeListWithSplit(list, "_");
    }

    public static List<String> decodeStrListWithUnderline(String items) {
        return decodeListWithSplit(items, "_", String::valueOf);
    }

    public static <T> List<T> addList(List<T> a, List<T> b) {
        List<T> res = null;
        Set<T> aSet = MiscUtil.list2Set(a);
        Set<T> bSet = MiscUtil.list2Set(b);

        log.info("{}addList a={},b={}", TAG, a, b);

        if (MiscUtil.isNotEmpty(aSet) && MiscUtil.isNotEmpty(bSet)) {
            aSet.addAll(bSet);
            res = MiscUtil.set2List(aSet);
        } else if (MiscUtil.isNotEmpty(aSet)) {
            res = a;
        } else if (MiscUtil.isNotEmpty(bSet)) {
            res = b;
        }

        return res;
    }

    public static <T> List<T> subtractionList(List<T> a, List<T> b) {
        log.info("{}subtractionList a={},b={}", TAG, a, b);

        if (MiscUtil.isNotEmpty(a) && MiscUtil.isNotEmpty(b)) {
            a.removeIf(b::contains);
        }

        return a;
    }
}
