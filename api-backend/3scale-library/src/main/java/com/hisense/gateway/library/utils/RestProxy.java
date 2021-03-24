///*
// * Licensed Materials - Property of tenxcloud.com
// * (C) Copyright 2019 TenxCloud. All Rights Reserved.
// */
//
//package com.hisense.gateway.library.utils;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.*;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Map;
//
///**
// * RestProxy
// *
// * @author wangjisnhan
// * @version v1.0
// * @date 2019-11-20 19:18
// */
//@Component
//public class RestProxy {
//    @Autowired
//    RestTemplate restTemplate;
//
//    public <RequestBody, ResponseBody> ResponseBody getForObject(String url, RequestBody payload, Class<ResponseBody> responseType){
//        HttpEntity<RequestBody> request = new HttpEntity<>(payload, headers(null));
//        return restTemplate.exchange(url, HttpMethod.GET, request, responseType).getBody();
//    }
//
//
//
//    public <RequestBody, ResponseBody> ResponseBody postForObject(String url, RequestBody payload, Class<ResponseBody> responseType){
//        HttpEntity<RequestBody> request = new HttpEntity<>(payload, headers(null));
//        return restTemplate.exchange(url, HttpMethod.POST, request, responseType).getBody();
//    }
//
//    public <RequestBody, ResponseBody> ResponseBody postFormDataForObject(String url, RequestBody payload, Class<ResponseBody> responseType){
//        HttpEntity<RequestBody> request = new HttpEntity<>(payload, headers(null));
//
//        /* 注意：必须 http、https……开头，不然报错，浏览器地址栏不加 http 之类不出错是因为浏览器自动帮你补全了 */
//        HttpHeaders headers = new HttpHeaders();
//        /* 这个对象有add()方法，可往请求头存入信息 */
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        /* 解决中文乱码的关键 , 还有更深层次的问题 关系到 StringHttpMessageConverter，先占位，以后补全*/
//        HttpEntity<RequestBody> entity = new HttpEntity<RequestBody>(payload, headers);
//
//        return restTemplate.exchange(url, HttpMethod.POST, entity, responseType).getBody();
//    }
//
//    public <RequestBody, ResponseBody> ResponseEntity<ResponseBody> post(String url, RequestBody payload, Class<ResponseBody> responseType){
//        HttpEntity<RequestBody> request = new HttpEntity<>(payload, headers(null));
//        return restTemplate.exchange(url, HttpMethod.POST, request, responseType);
//    }
//
//    public <RequestBody, ResponseBody> ResponseEntity<ResponseBody> put(String url, RequestBody payload, Class<ResponseBody> responseType){
//        HttpEntity<RequestBody> request = new HttpEntity<>(payload, headers(null));
//        return restTemplate.exchange(url, HttpMethod.PUT, request, responseType);
//    }
//
//    public HttpHeaders headers(Map<String, Object> externalHeaders) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        return headers;
//    }
//
//}
