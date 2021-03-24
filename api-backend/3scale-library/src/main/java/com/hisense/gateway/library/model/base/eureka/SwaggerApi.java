package com.hisense.gateway.library.model.base.eureka;

import io.swagger.models.Swagger;

public class SwaggerApi extends Swagger {
    public SwaggerApi() {
    }

    public SwaggerApi(Swagger swagger) {
        this.setSwagger(swagger.getSwagger());
        this.setHost(swagger.getHost());
        this.setBasePath(swagger.getBasePath());
        this.setTags(swagger.getTags());
        this.setSchemes(swagger.getSchemes());
        this.setConsumes(swagger.getConsumes());
        this.setProduces(swagger.getProduces());
        this.setSecurity(swagger.getSecurity());
        this.setPaths(swagger.getPaths());
        this.setSecurityDefinitions(swagger.getSecurityDefinitions());
        this.setDefinitions(swagger.getDefinitions());
        this.setParameters(swagger.getParameters());
        this.setResponses(swagger.getResponses());
        this.setExternalDocs(swagger.getExternalDocs());
    }

    public void removePath(String path) {
        if (paths != null) {
            paths.remove(path);
        }
    }

    @Override
    public String toString() {
        return "SwaggerApi{" +
                "swagger='" + swagger + '\'' +
                ", info=" + info +
                ", host='" + host + '\'' +
                ", basePath='" + basePath + '\'' +
                ", tags=" + tags +
                ", schemes=" + schemes +
                ", consumes=" + consumes +
                ", produces=" + produces +
                ", security=" + security +
                ", paths=" + paths +
                ", securityDefinitions=" + securityDefinitions +
                ", definitions=" + definitions +
                ", parameters=" + parameters +
                ", responses=" + responses +
                ", externalDocs=" + externalDocs +
                ", vendorExtensions=" + vendorExtensions +
                '}';
    }
}
