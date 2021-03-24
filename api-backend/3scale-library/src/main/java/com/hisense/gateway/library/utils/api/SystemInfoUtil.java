package com.hisense.gateway.library.utils.api;

import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.utils.CommonUtil;

import java.util.List;

/**
 * @author guilai.ming 2020/09/10
 */
public class SystemInfoUtil {
    public static List<String> getProjectList(String projectIds) {
        return CommonUtil.decodeStrListWithUnderline(projectIds);
    }
}
