
package com.hisense.gateway.library.utils.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.tenxcloud.gateway.developer.utils.client package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _LdapQueryInfoParamsResponse_QNAME = new QName("http://services.soap.ws.esc.para.com/", "ldapQueryInfoParamsResponse");
    private final static QName _LdapQueryInfoResponse_QNAME = new QName("http://services.soap.ws.esc.para.com/", "ldapQueryInfoResponse");
    private final static QName _LdapQueryInfoParams_QNAME = new QName("http://services.soap.ws.esc.para.com/", "ldapQueryInfoParams");
    private final static QName _LdapQueryInfo_QNAME = new QName("http://services.soap.ws.esc.para.com/", "ldapQueryInfo");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.tenxcloud.gateway.developer.utils.client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link LdapQueryInfo }
     * 
     */
    public LdapQueryInfo createLdapQueryInfo() {
        return new LdapQueryInfo();
    }

    /**
     * Create an instance of {@link LdapQueryInfoParams }
     * 
     */
    public LdapQueryInfoParams createLdapQueryInfoParams() {
        return new LdapQueryInfoParams();
    }

    /**
     * Create an instance of {@link LdapQueryInfoResponse }
     * 
     */
    public LdapQueryInfoResponse createLdapQueryInfoResponse() {
        return new LdapQueryInfoResponse();
    }

    /**
     * Create an instance of {@link LdapQueryInfoParamsResponse }
     * 
     */
    public LdapQueryInfoParamsResponse createLdapQueryInfoParamsResponse() {
        return new LdapQueryInfoParamsResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LdapQueryInfoParamsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.soap.ws.esc.para.com/", name = "ldapQueryInfoParamsResponse")
    public JAXBElement<LdapQueryInfoParamsResponse> createLdapQueryInfoParamsResponse(LdapQueryInfoParamsResponse value) {
        return new JAXBElement<LdapQueryInfoParamsResponse>(_LdapQueryInfoParamsResponse_QNAME, LdapQueryInfoParamsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LdapQueryInfoResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.soap.ws.esc.para.com/", name = "ldapQueryInfoResponse")
    public JAXBElement<LdapQueryInfoResponse> createLdapQueryInfoResponse(LdapQueryInfoResponse value) {
        return new JAXBElement<LdapQueryInfoResponse>(_LdapQueryInfoResponse_QNAME, LdapQueryInfoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LdapQueryInfoParams }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.soap.ws.esc.para.com/", name = "ldapQueryInfoParams")
    public JAXBElement<LdapQueryInfoParams> createLdapQueryInfoParams(LdapQueryInfoParams value) {
        return new JAXBElement<LdapQueryInfoParams>(_LdapQueryInfoParams_QNAME, LdapQueryInfoParams.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LdapQueryInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services.soap.ws.esc.para.com/", name = "ldapQueryInfo")
    public JAXBElement<LdapQueryInfo> createLdapQueryInfo(LdapQueryInfo value) {
        return new JAXBElement<LdapQueryInfo>(_LdapQueryInfo_QNAME, LdapQueryInfo.class, null, value);
    }

}
