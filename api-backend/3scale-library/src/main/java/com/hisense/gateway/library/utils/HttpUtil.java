package com.hisense.gateway.library.utils;

import com.hisense.gateway.library.model.Result;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hisense.gateway.library.constant.BaseConstants.TAG;


public class HttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static final CloseableHttpClient httpclient = HttpClients.createDefault();


    public static String sendPost(String url, String json) throws IOException {
        logger.info("request, url: {}, body: {}", url, json);

        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);

        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(entity);
        CloseableHttpResponse response = httpclient.execute(httppost);
        HttpEntity entityRes = response.getEntity();

        String ret = EntityUtils.toString(entityRes);

        logger.info("get response: {}", ret);

        return ret;
    }


    /**
     * 发送HttpPost请求，参数为map
     *
     * @param url
     * @param map
     * @return
     */
    public static String sendPost(String url, Map<String, String> map){
        List<NameValuePair> formParams = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(entity);
        logger.info("start invoke HttpUtil.sendPost,entity is {},httppost is {}",
                entity,httppost);
        HttpClient httpClient = null;
        String result = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httppost);
                logger.info("invoke HttpUtil.sendPost,response is {}",
                        response);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            } else {
                CloseableHttpResponse response = httpclient.execute(httppost);
                logger.info("invoke HttpUtil.sendPost,response is {}",
                        response);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;
    }


    public static String sendPostByJson(String url, Map<String, String> map){
        String result = null;
        List<NameValuePair> formParams = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            formParams.add(new BasicNameValuePair(entry.getKey(),String.valueOf(entry.getValue())));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        CloseableHttpResponse closeableHttpResponse;
        try {
            httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            httpPost.setEntity(entity);
            closeableHttpResponse = closeableHttpClient.execute(httpPost);
            result = EntityUtils.toString(closeableHttpResponse.getEntity(), "UTF-8");
        } catch (Exception e) {
            logger.error(String.format("url=%s,params=%s",url,map));
            logger.error("请求异常",e);
            return result;
        }finally {
            try {
                if(closeableHttpClient != null)closeableHttpClient.close();
            } catch (IOException e) {
                logger.error("closeableHttpClient close exception",e);
            }
        }
        return result;
    }



    /**
     * 发送sendPatch请求，参数为map
     *
     * @param url
     * @param map
     * @return
     */
    public static String sendPatch(String url, Map<String, String> map){
        List<NameValuePair> formParams = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        HttpPatch httpPatch = new HttpPatch(url);
        httpPatch.setEntity(entity);
        logger.info("start invoke HttpUtil.httpPatch,entity is {},httpPatch is {}",
                entity,httpPatch);
        HttpClient httpClient = null;
        String result = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpPatch);
                logger.info("invoke HttpUtil.httpPatch,response is {}",
                        response);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            } else {
                CloseableHttpResponse response = httpclient.execute(httpPatch);
                logger.info("invoke HttpUtil.httpPatch,response is {}",
                        response);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;

    }
    /**
     * 发送HttpGet请求
     *
     * @param url
     * @return
     */
    public static String sendGet(String url){

        HttpGet httpget = new HttpGet(url);

        HttpClient httpClient = null;
        String result = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpget);
                logger.info("invoke HttpUtil.sendGet,response is {}",
                        response);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            } else {
                CloseableHttpResponse response = httpclient.execute(httpget);
                logger.info("invoke HttpUtil.sendGet,response is {}",
                        response);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;

    }

    public static String sendGet2(String url){
        HttpGet httpget = new HttpGet(url);
        HttpClient httpClient = null;
        String result = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpget);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            } else {
                CloseableHttpResponse response = httpclient.execute(httpget);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;

    }

    /**
     * 发送HttpPut请求
     *
     * @param url
     * @param map
     * @return
     */
    public static String sendPut(String url, Map<String, String> map){

        HttpPut httpPut = new HttpPut(url);

        List<NameValuePair> formParams = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        httpPut.setEntity(entity);

        HttpClient httpClient = null;
        String result = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpPut);
                logger.info("invoke HttpUtil.sendPut,response is {}",
                        response);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            } else {
                CloseableHttpResponse response = httpclient.execute(httpPut);
                logger.info("invoke HttpUtil.sendPut,response is {}",
                        response);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;

    }

    /**
     * 发送HttpDelete请求
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String sendDel(String url){

        HttpDelete httpDel = new HttpDelete(url);
        HttpClient httpClient = null;
        String result = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpDel);
                logger.info("invoke HttpUtil.sendDel,response is {}",
                        response);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            } else {
                CloseableHttpResponse response = httpclient.execute(httpDel);
                logger.info("invoke HttpUtil.sendDel,response is {}",
                        response);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;

    }

    public static Map<String, Object> sendGetAndGetCode(String url, List<Map<String, String>> header)  {
        Map<String, Object> result = new HashMap<>(3);
        HttpGet httpget = new HttpGet(url);
        if (null != header && header.size() > 0) {
            for (Map<String, String> headerMap : header) {
                String param = headerMap.get("param");
                String value = headerMap.get("value");
                if(param==null){
                    if(!CollectionUtils.isEmpty(headerMap)){
                        for(String key:headerMap.keySet()){
                            httpget.setHeader(key, headerMap.get(key));
                        }
                    }
                }else{
                    httpget.setHeader(param, value);
                }
            }
        }
        HttpClient httpClient = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpget);
                HttpEntity entityRes = response.getEntity();
                result.put("code", response.getStatusLine().getStatusCode());
                result.put("data", EntityUtils.toString(entityRes));
                result.put("contentType", response.getEntity().getContentType());
            } else {
                CloseableHttpResponse response = httpclient.execute(httpget);
                //byte[] byteData = EntityUtils.toByteArray(response.getEntity());
                HttpEntity entityRes = response.getEntity();
                result.put("code", response.getStatusLine().getStatusCode());
                result.put("data", EntityUtils.toString(entityRes));
                result.put("contentType", response.getEntity().getContentType());
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;
    }

    public static Map<String, Object> sendPostJsonAndGetCode(String url, String paramJson,List<Map<String, String>> header) {
        logger.info("request, url: {}, body: {}", url, paramJson);
        Map<String,Object> result = new HashMap<>(3);
        HttpPost httppost = new HttpPost(url);
        if (null != header && header.size() > 0) {
            for (Map<String, String> headerMap : header) {
                String param = headerMap.get("param");
                String value = headerMap.get("value");
                if(param==null){
                    if(!CollectionUtils.isEmpty(headerMap)){
                        for(String key:headerMap.keySet()){
                            httppost.setHeader(key, headerMap.get(key));
                        }
                    }
                }else{
                    httppost.setHeader(param, value);
                }
            }
        }
        StringEntity entity = new StringEntity(paramJson, ContentType.APPLICATION_JSON);
        httppost.setEntity(entity);
        HttpClient httpClient = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httppost);
                HttpEntity entityRes = response.getEntity();
                String ret = EntityUtils.toString(entityRes);
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",ret);
                result.put("contentType", response.getEntity().getContentType());
            } else {
                CloseableHttpResponse response = httpclient.execute(httppost);
                HttpEntity entityRes = response.getEntity();
                String ret = EntityUtils.toString(entityRes);
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",ret);
                result.put("contentType", response.getEntity().getContentType());
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }

        return result;
    }

    public static Map<String, Object> sendPostFormAndGetCode(String url, Map<String, Object> params,
                                                             List<Map<String, String>> header) {
        Map<String,Object> result = new HashMap<>(3);
        List<NameValuePair> formParams = new ArrayList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            formParams.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        HttpPost httppost = new HttpPost(url);
        if (null != header && header.size() > 0) {
            for (Map<String, String> headerMap : header) {
                String value = headerMap.get("value");
                String param = headerMap.get("param");
                if(param==null){
                    if(!CollectionUtils.isEmpty(headerMap)){
                        for(String key:headerMap.keySet()){
                            httppost.setHeader(key, headerMap.get(key));
                        }
                    }
                }else{
                    httppost.setHeader(param, value);
                }
            }
        }
        httppost.setEntity(entity);
        HttpClient httpClient = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httppost);
                HttpEntity entityRes = response.getEntity();
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",EntityUtils.toString(entityRes));
                result.put("contentType", response.getEntity().getContentType());
                return result;
            } else {
                CloseableHttpResponse response = httpclient.execute(httppost);
                HttpEntity entityRes = response.getEntity();
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",EntityUtils.toString(entityRes));
                result.put("contentType", response.getEntity().getContentType());
                return result;
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;
    }

    public static Map<String, Object> sendPutFormAndGetCode(String url, Map<String, Object> params,
                                                            List<Map<String, String>> header){
        Map<String,Object> result = new HashMap<>(3);
        HttpPut httpPut = new HttpPut(url);
        if (null != header && header.size() > 0) {
            for (Map<String, String> headerMap : header) {
                String param = headerMap.get("param");
                String value = headerMap.get("value");
                if(param==null){
                    if(!CollectionUtils.isEmpty(headerMap)){
                        for(String key:headerMap.keySet()){
                            httpPut.setHeader(key, headerMap.get(key));
                        }
                    }
                }else{
                    httpPut.setHeader(param, value);
                }
            }
        }
        List<NameValuePair> formParams = new ArrayList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            formParams.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        httpPut.setEntity(entity);
        HttpClient httpClient = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpPut);
                HttpEntity entityRes = response.getEntity();
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",EntityUtils.toString(entityRes));
                result.put("contentType", response.getEntity().getContentType());
            } else {
                CloseableHttpResponse response = httpclient.execute(httpPut);
                HttpEntity entityRes = response.getEntity();
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",EntityUtils.toString(entityRes));
                result.put("contentType", response.getEntity().getContentType());
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;

    }

    public static Map<String, Object> sendDelAndGetCode(String url, List<Map<String, String>> header) {
        Map<String,Object> result = new HashMap<>(3);
        HttpDelete httpDel = new HttpDelete(url);
        if (null != header && header.size() > 0) {
            for (Map<String, String> headerMap : header) {
                String param = headerMap.get("param");
                String value = headerMap.get("value");
                if(param==null){
                    if(!CollectionUtils.isEmpty(headerMap)){
                        for(String key:headerMap.keySet()){
                            httpDel.setHeader(key, headerMap.get(key));
                        }
                    }
                }else{
                    httpDel.setHeader(param, value);
                }
            }
        }
        HttpClient httpClient = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpDel);
                HttpEntity entityRes = response.getEntity();
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",EntityUtils.toString(entityRes));
                result.put("contentType", response.getEntity().getContentType());
                return result;
            } else {
                CloseableHttpResponse response = httpclient.execute(httpDel);
                HttpEntity entityRes = response.getEntity();
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",EntityUtils.toString(entityRes));
                result.put("contentType", response.getEntity().getContentType());
                return result;
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;

    }

    public static Map<String, Object> sendPutJsonAndGetCode(String url, String paramJson,
                                                            List<Map<String, String>> header) {
        logger.info("request, url: {}, body: {}", url, paramJson);
        Map<String,Object> result = new HashMap<>(3);
        HttpPut httpPut = new HttpPut(url);
        if (null != header && header.size() > 0) {
            for (Map<String, String> headerMap : header) {
                String param = headerMap.get("param");
                String value = headerMap.get("value");
                if(param==null){
                    if(!CollectionUtils.isEmpty(headerMap)){
                        for(String key:headerMap.keySet()){
                            httpPut.setHeader(key, headerMap.get(key));
                        }
                    }
                }else{
                    httpPut.setHeader(param, value);
                }
            }
        }
        StringEntity entity = new StringEntity(paramJson, ContentType.APPLICATION_JSON);
        httpPut.setEntity(entity);
        HttpClient httpClient = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpPut);
                HttpEntity entityRes = response.getEntity();
                String ret = EntityUtils.toString(entityRes);
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",ret);
                result.put("contentType", response.getEntity().getContentType());
            } else {
                CloseableHttpResponse response = httpclient.execute(httpPut);
                HttpEntity entityRes = response.getEntity();
                String ret = EntityUtils.toString(entityRes);
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",ret);
                result.put("contentType", response.getEntity().getContentType());
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;
    }

    public static String sendGetAndAuthorization(String url, String authorization) {
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("Authorization",authorization);
        HttpClient  httpClient = null;
        String result = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpget);
                logger.info("invoke HttpUtil.sendGet,response is {}",
                        response);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            } else {
                CloseableHttpResponse response = httpclient.execute(httpget);
                logger.info("invoke HttpUtil.sendGet,response is {}",
                        response);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;

    }

    public static Map<String, Object> sendPostXmlAndGetCode(String url, String paramXml, List<Map<String, String>> header) {
        logger.info("request, url: {}, body: {}", url, paramXml);
        Map<String,Object> result = new HashMap<>(3);
        HttpPost httppost = new HttpPost(url);
        if (null != header && header.size() > 0) {
            for (Map<String, String> headerMap : header) {
                String param = headerMap.get("param");
                String value = headerMap.get("value");
                if(param==null){
                    if(!CollectionUtils.isEmpty(headerMap)){
                        for(String key:headerMap.keySet()){
                            httppost.setHeader(key, headerMap.get(key));
                        }
                    }
                }else{
                    httppost.setHeader(param, value);
                }
            }
        }
        StringEntity entity = new StringEntity(paramXml, ContentType.APPLICATION_XML);
        httppost.setEntity(entity);
        HttpClient httpClient = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httppost);
                HttpEntity entityRes = response.getEntity();
                String ret = EntityUtils.toString(entityRes);
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",ret);
                result.put("contentType", response.getEntity().getContentType());
            } else {
                CloseableHttpResponse response = httpclient.execute(httppost);
                HttpEntity entityRes = response.getEntity();
                String ret = EntityUtils.toString(entityRes);
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",ret);
                result.put("contentType", response.getEntity().getContentType());
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;
    }

    public static Map<String, Object> sendPutXmlAndGetCode(String url, String paramXml,
                                                           List<Map<String, String>> header){
        logger.info("request, url: {}, body: {}", url, paramXml);
        Map<String,Object> result = new HashMap<>(3);
        HttpPut httpPut = new HttpPut(url);
        if (null != header && header.size() > 0) {
            for (Map<String, String> headerMap : header) {
                String param = headerMap.get("param");
                String value = headerMap.get("value");
                if(param==null){
                    if(!CollectionUtils.isEmpty(headerMap)){
                        for(String key:headerMap.keySet()){
                            httpPut.setHeader(key, headerMap.get(key));
                        }
                    }
                }else{
                    httpPut.setHeader(param, value);
                }
            }
        }
        StringEntity entity = new StringEntity(paramXml, ContentType.APPLICATION_XML);
        httpPut.setEntity(entity);
        HttpClient httpClient = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpPut);
                HttpEntity entityRes = response.getEntity();
                String ret = EntityUtils.toString(entityRes);
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",ret);
                result.put("contentType", response.getEntity().getContentType());
            } else {
                CloseableHttpResponse response = httpclient.execute(httpPut);
                HttpEntity entityRes = response.getEntity();
                String ret = EntityUtils.toString(entityRes);
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",ret);
                result.put("contentType", response.getEntity().getContentType());
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;

    }

    public static Map<String, Object> sendPatchJsonAndGetCode(String url, String paramJson,
                                                              List<Map<String, String>> header) {
        logger.info("request, url: {}, body: {}", url, paramJson);
        Map<String,Object> result = new HashMap<>(3);
        HttpPatch httpPatch = new HttpPatch(url);
        if (null != header && header.size() > 0) {
            for (Map<String, String> headerMap : header) {
                String param = headerMap.get("param");
                String value = headerMap.get("value");
                if(param==null){
                    if(!CollectionUtils.isEmpty(headerMap)){
                        for(String key:headerMap.keySet()){
                            httpPatch.setHeader(key, headerMap.get(key));
                        }
                    }
                }else{
                    httpPatch.setHeader(param, value);
                }
            }
        }
        StringEntity entity = new StringEntity(paramJson, ContentType.APPLICATION_JSON);
        httpPatch.setEntity(entity);
        HttpClient httpClient = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpPatch);
                HttpEntity entityRes = response.getEntity();
                String ret = EntityUtils.toString(entityRes);
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",ret);
                result.put("contentType", response.getEntity().getContentType());
            } else {
                CloseableHttpResponse response = httpclient.execute(httpPatch);
                HttpEntity entityRes = response.getEntity();
                String ret = EntityUtils.toString(entityRes);
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",ret);
                result.put("contentType", response.getEntity().getContentType());
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;
    }

    public static Map<String, Object> sendPatchFormAndGetCode(String url, Map<String, Object> parameterMap,
                                                              List<Map<String, String>> header){
        Map<String,Object> result = new HashMap<>(3);
        logger.info("request, url: {}, body: {}", url, parameterMap);
        List<NameValuePair> formParams = new ArrayList<>();
        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            formParams.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        HttpPatch httpPatch = new HttpPatch(url);
        if (null != header && header.size() > 0) {
            for (Map<String, String> headerMap : header) {
                String param = headerMap.get("param");
                String value = headerMap.get("value");
                if(param==null){
                    if(!CollectionUtils.isEmpty(headerMap)){
                        for(String key:headerMap.keySet()){
                            httpPatch.setHeader(key, headerMap.get(key));
                        }
                    }
                }else{
                    httpPatch.setHeader(param, value);
                }
            }
        }
        httpPatch.setEntity(entity);
        logger.info("start invoke HttpUtil.httpPatch,entity is {},httpPatch is {}",
                entity,httpPatch);
        HttpClient httpClient = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpPatch);
                logger.info("invoke HttpUtil.httpPatch,response is {}",
                        response);
                HttpEntity entityRes = response.getEntity();
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",EntityUtils.toString(entityRes));
                result.put("contentType", response.getEntity().getContentType());
            } else {
                CloseableHttpResponse response = httpclient.execute(httpPatch);
                logger.info("invoke HttpUtil.httpPatch,response is {}",
                        response);
                HttpEntity entityRes = response.getEntity();
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",EntityUtils.toString(entityRes));
                result.put("contentType", response.getEntity().getContentType());
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;
    }

    public static Map<String, Object> sendPatchXmlAndGetCode(String url, String paramXml,
                                                             List<Map<String, String>> header){
        logger.info("request, url: {}, body: {}", url, paramXml);
        Map<String,Object> result = new HashMap<>(3);
        HttpPatch httpPatch = new HttpPatch(url);
        if (null != header && header.size() > 0) {
            for (Map<String, String> headerMap : header) {
                String value = headerMap.get("value");
                String param = headerMap.get("param");
                if(param==null){
                    if(!CollectionUtils.isEmpty(headerMap)){
                        for(String key:headerMap.keySet()){
                            httpPatch.setHeader(key, headerMap.get(key));
                        }
                    }
                }else{
                    httpPatch.setHeader(param, value);
                }
            }
        }
        StringEntity entity = new StringEntity(paramXml, ContentType.APPLICATION_XML);
        httpPatch.setEntity(entity);
        HttpClient httpClient = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpPatch);
                HttpEntity entityRes = response.getEntity();
                String ret = EntityUtils.toString(entityRes);
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",ret);
                result.put("contentType", response.getEntity().getContentType());
            } else {
                CloseableHttpResponse response = httpclient.execute(httpPatch);
                HttpEntity entityRes = response.getEntity();
                String ret = EntityUtils.toString(entityRes);
                result.put("code",response.getStatusLine().getStatusCode());
                result.put("data",ret);
                result.put("contentType", response.getEntity().getContentType());
            }
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;
    }

    public static String sendGetJson(String url) {
        HttpGet httpGet = new HttpGet(url);
        HttpClient httpClient = null;
        String result = null;
        try {
            if (url.startsWith("https")) {
                httpClient = new SSLClient();
                HttpResponse response = httpClient.execute(httpGet);
                logger.debug("invoke ssl httputil.sendGet,response is {}", response);
                HttpEntity entityRes = response.getEntity();
                result = EntityUtils.toString(entityRes);
            } else {
                httpGet.setHeader("Accept", "application/json");
                httpGet.setHeader("Content-type", "application/json");
                CloseableHttpResponse response = httpclient.execute(httpGet);

                if (response.getEntity().isChunked()) {
                    BufferedInputStream inputStream = new BufferedInputStream(response.getEntity().getContent());
                    int len = 0;
                    StringBuilder stringBuilder = new StringBuilder();
                    byte[] buf = new byte[1024];
                    while ((len = inputStream.read(buf, 0, buf.length)) != -1) {
                        stringBuilder.append(new String(buf, 0, len));
                    }
                    logger.debug("{}invoke httputil.sendGet,response is {}", TAG, stringBuilder.toString());
                    inputStream.close();
                    result = stringBuilder.toString();
                } else {
                    result = EntityUtils.toString(response.getEntity());
                }
            };
        } catch (Exception e) {
            logger.error("请求异常",e);
        }finally {
            try {
                if(httpClient!=null){
                    ((SSLClient) httpClient).close();
                }
            }catch (Exception e){
                logger.error("in close exception",e);
            }
        }
        return result;

    }

    public static String buildExceptionMessage(Exception exception) {
        if (exception.getMessage().contains("Connection refused")) {
            return "服务端已下线,连接被拒绝";
        }
        return exception.getMessage();
    }

    /**
     * 发送httpPost请求，参数为json,
     * @param url
     * @param json
     * @return
     * @throws IOException
     */
    public static String sendPostAndHeader(String url, String json ,Map<String,String> header) throws IOException {
        logger.info("request, url: {}, body: {}", url, json);

        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);

        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(entity);
        httppost.setHeader("appKey", header.get("appKey"));
        httppost.setHeader("OperationCode", header.get("OperationCode"));
        CloseableHttpResponse response = httpclient.execute(httppost);
        HttpEntity entityRes = response.getEntity();

        String ret = EntityUtils.toString(entityRes);

        logger.info("get response: {}", ret);

        return ret;
    }

    public static Result<Object> sendPostAndHeader1(String url, Map<String, Object> params , Map<String,String> header) {
        Result<Object> result = new Result<>(Result.OK,"请求成功！",null);
        logger.info("request, url: {}, body: {}", url, params);

        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpMethod method = HttpMethod.POST;
        // 以表单的方式提交
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("content-type", "application/json;charset=UTF-8");
        headers.set("appKey", header.get("appKey"));
        headers.set("OperationCode", header.get("OperationCode"));
        //将请求头部和参数合成一个请求
        org.springframework.http.HttpEntity<Map<String, Object>> requestEntity = new org.springframework.http.HttpEntity<>(params, headers);
        //执行HTTP请求，将返回的结构使用ResultVO类格式化
        ResponseEntity<Object> response = client.exchange(url, method, requestEntity, Object.class);
        result.setData(response.getBody());
        return result;
    }
}
