/*
 * 2020-09-06 @author guilai.ming
 */
package com.hisense.gateway.library.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.constant.ParameterContentType;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.monitor.ApiCastLog;
import com.hisense.gateway.library.model.base.monitor.ApiLogQueryFull;
import com.hisense.gateway.library.model.base.monitor.ApiLogQuerySingle;
import com.hisense.gateway.library.model.base.monitor.ApiLogRecord;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import com.hisense.gateway.library.repository.PublishApiRepository;
import com.hisense.gateway.library.repository.PublishApplicationRepository;
import com.hisense.gateway.library.service.ApiLogService;
import com.hisense.gateway.library.service.DebuggingService;
import com.hisense.gateway.library.service.ElasticSearchService;
import com.hisense.gateway.library.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hisense.gateway.library.constant.BaseConstants.TAG;

@Slf4j
@Service
public class ApiLogServiceImpl implements ApiLogService {

    @Autowired
    ElasticSearchService elasticSearchService;
    @Autowired
    PublishApiRepository publishApiRepository;
    @Autowired
    DebuggingService debuggingService;
    @Autowired
    PublishApplicationRepository applicationRepository;
    @Override
    public Result<Object> recall(ApiLogQuerySingle apiLogQuerySingle) {
        Result<Object>  result = new Result<>(Result.OK,"重发成功！",null);
        Result<ApiCastLog> apiCastLogResult = elasticSearchService.queryForSingle(apiLogQuerySingle);
        if(Result.FAIL.equals(apiCastLogResult.getCode())){
            result.setError(Result.FAIL,apiCastLogResult.getMsg());
            return result;
        }
        //重新调用
        ApiCastLog  apiLog =  apiCastLogResult.getData();
        if(null == apiLog){
            result.setError(Result.FAIL,"查询的调用记录为空");
            return result;
        }
        try{
            PublishApi api = publishApiRepository.findOne(apiLogQuerySingle.getApiId());
            String path =apiLog.getReqHeaders().get("x-forwarded-proto")+"://"+apiLog.getReqHeaders().get("x-forwarded-host")+":"
                    +apiLog.getReqHeaders().get("x-forwarded-port")+ apiLog.getRequestUri();
            log.info("{}path={}", TAG, path);
            Map<String, Object> resultMap = new HashMap<>();
            List<Map<String, String>> header = new ArrayList<>();
            header.add( apiLog.getReqHeaders());
            String method = apiLog.getRequestMethod();
            String paramJson =  apiLog.getRequestBody();
            //处理并拿到请求入参
            Map<String, Object> paramsMap = new HashMap<String, Object>();
            if(StringUtils.isNotBlank(apiLog.getRequestBody())){
                paramsMap = JSONObject.parseObject(apiLog.getRequestBody(),Map.class);
            }
            if ("GET".equals(method)) {
                resultMap = HttpUtil.sendGetAndGetCode(path, header);
            } else if ("POST".equals(method)) {
                if (ParameterContentType.JSON.equalsIgnoreCase(apiLog.getRespContentType())) {
                    log.info("{}paramJson={},header={}", TAG, paramJson,header);
                    resultMap = HttpUtil.sendPostJsonAndGetCode(path, paramJson, header);
                } else if (ParameterContentType.FORM.contains(apiLog.getRespContentType())) {
                    log.info("{}parameterMap={}", TAG, apiLog.getRequestBody());
                    resultMap = HttpUtil.sendPostFormAndGetCode(path, paramsMap,header);
                } else if (ParameterContentType.XML.equalsIgnoreCase(apiLog.getRespContentType())) {

                    resultMap = HttpUtil.sendPostXmlAndGetCode(path, apiLog.getRequestBody(),header);
                }
            } else if ("PUT".equals(method)) {
                if (ParameterContentType.JSON.equalsIgnoreCase(apiLog.getRespContentType())) {
                    resultMap = HttpUtil.sendPutJsonAndGetCode(path, paramJson, header);
                } else if (ParameterContentType.FORM.contains(apiLog.getRespContentType())) {
                    resultMap = HttpUtil.sendPutFormAndGetCode(path, paramsMap,header);
                } else if (ParameterContentType.XML.equalsIgnoreCase(apiLog.getRespContentType())) {
                    String paramXml = apiLog.getRequestBody();
                    resultMap = HttpUtil.sendPutXmlAndGetCode(path, paramXml, header);
                }
            } else if ("PATCH".equals(method)) {
                if (ParameterContentType.JSON.equalsIgnoreCase(apiLog.getRespContentType())) {
                    resultMap = HttpUtil.sendPatchJsonAndGetCode(path, paramJson, header);
                } else if (ParameterContentType.FORM.contains(apiLog.getRespContentType())) {
                    resultMap = HttpUtil.sendPatchFormAndGetCode(path, paramsMap,header);
                } else if (ParameterContentType.XML.equalsIgnoreCase(apiLog.getRespContentType())) {
                    String paramXml = apiLog.getRequestBody();
                    resultMap = HttpUtil.sendPatchXmlAndGetCode(path, paramXml, header);
                }
            } else if ("DELETE".equals(method)) {
                resultMap = HttpUtil.sendDelAndGetCode(path,header);
            }
            resultMap.put("state", true);
            result.setData(resultMap.get("data"));
        }catch (Exception e){
            log.error("重发异常：",e);
            result.setError(Result.FAIL,"重发异常："+e.getMessage());
            return result;
        }
        return result;
    }

    @Override
    public void download(HttpServletResponse response, ApiLogQueryFull apiLogQueryFull) {
        Result<Page<ApiLogRecord>> result = elasticSearchService.queryForPage(apiLogQueryFull);
        List<ApiLogRecord> apiLogRecord = result.getData().getContent();
        createFile(apiLogRecord,response,12);
    }

    //创建excel文件
    private void createFile(List<ApiLogRecord> list, HttpServletResponse response,int cols){
        XSSFWorkbook wb = new XSSFWorkbook();
        // 创建工作表
        XSSFSheet sheet = wb.createSheet("日志详情");

        XSSFCellStyle style = wb.createCellStyle();
        sheet.setDefaultColumnWidth(15);//设置列宽
        style.setAlignment(HorizontalAlignment.CENTER); // 水平居中
        style.setBorderBottom(BorderStyle.THIN); // 下边框
        style.setBorderLeft(BorderStyle.THIN);// 左边框
        style.setBorderTop(BorderStyle.THIN);// 上边框
        style.setBorderRight(BorderStyle.THIN);// 右边框
        style.setVerticalAlignment(VerticalAlignment.CENTER);//垂直居中
        int rows = list.size();
        // 创建单元格
        for (int i = 0; i < rows + 1; i++) {
            XSSFRow row = sheet.createRow(i);
            for (int j = 0; j < cols; j++) {
                XSSFCell cell = row.createCell(j);
                cell.setCellStyle(style);
            }
        }
        //定义数据
        for (int i = 0; i < rows + 1; i++) {
            for (int j = 0; j < cols; j++) {
                if (i == 0) {
                    switch (j) {
                        case 0:
                            sheet.getRow(i).getCell(j).setCellValue("API名称");
                            break;
                        case 1:
                            sheet.getRow(i).getCell(j).setCellValue("请求时间");
                            break;
                        case 2:
                            sheet.getRow(i).getCell(j).setCellValue("请求方式");
                            break;
                        case 3:
                            sheet.getRow(i).getCell(j).setCellValue("路由");
                            break;
                        case 4:
                            sheet.getRow(i).getCell(j).setCellValue("调用系统");
                            break;
                        case 5:
                            sheet.getRow(i).getCell(j).setCellValue("接口系统");
                            break;
                        case 6:
                            sheet.getRow(i).getCell(j).setCellValue("请求头");
                            break;
                        case 7:
                            sheet.getRow(i).getCell(j).setCellValue("请求体");
                            break;
                        case 8:
                            sheet.getRow(i).getCell(j).setCellValue("输出参数");
                            break;
                        case 9:
                            sheet.getRow(i).getCell(j).setCellValue("请求状态");
                            break;
                        case 10:
                            sheet.getRow(i).getCell(j).setCellValue("请求耗时（ms）");
                            break;
                        case 11:
                            sheet.getRow(i).getCell(j).setCellValue("IP地址");
                            break;
                        default:
                            break;
                    }

                } else {
                    switch (j) {
                        case 0:
                            sheet.getRow(i).getCell(j).setCellValue(list.get(i-1).getApiName());
                            break;
                        case 1:
                            sheet.getRow(i).getCell(j).setCellValue(list.get(i-1).getRequestTime());
                            break;
                        case 2:
                            sheet.getRow(i).getCell(j).setCellValue(list.get(i-1).getHttpMethod());
                            break;
                        case 3:
                            sheet.getRow(i).getCell(j).setCellValue(list.get(i-1).getHttpPattern());
                            break;
                        case 4:
                            sheet.getRow(i).getCell(j).setCellValue(list.get(i-1).getCallSystem());
                            break;
                        case 5:
                            sheet.getRow(i).getCell(j).setCellValue(list.get(i-1).getApiSystem());
                            break;
                        case 6:
                            sheet.getRow(i).getCell(j).setCellValue(JSONObject.toJSONString(list.get(i-1).getRequestHeader()));
                            break;
                        case 7:
                            sheet.getRow(i).getCell(j).setCellValue(JSONObject.toJSONString(list.get(i-1).getRequestBody()));
                            break;
                        case 8:
                            sheet.getRow(i).getCell(j).setCellValue(list.get(i-1).getResponseBody());
                            break;
                        case 9:
                            sheet.getRow(i).getCell(j).setCellValue(list.get(i-1).getHttpStatusCode());
                            break;
                        case 10:
                            sheet.getRow(i).getCell(j).setCellValue(list.get(i-1).getResponseTime());
                            break;
                        case 11:
                            sheet.getRow(i).getCell(j).setCellValue(list.get(i-1).getIpList());
                            break;
                        default:
                            break;
                    }
                }
            }

        }
        try {
            //FileOutputStream fout = new FileOutputStream("D:/日志.xls");
            wb.write(response.getOutputStream());
        }catch (Exception e){
            log.error("文件创建失败！");
        }finally {
            try {
                wb.close();
            }catch (Exception e){
                log.error("流关闭失败！");
            }
        }
    }
}
