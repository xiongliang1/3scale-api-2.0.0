
package com.hisense.gateway.library.utils.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>ldapQueryInfo complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="ldapQueryInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="appSecretId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="access_token" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="eidClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ldapQueryInfo", propOrder = {
    "appSecretId",
    "accessToken",
    "eidClass"
})
public class LdapQueryInfo {

    protected String appSecretId;
    @XmlElement(name = "access_token")
    protected String accessToken;
    protected String eidClass;

    /**
     * 获取appSecretId属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppSecretId() {
        return appSecretId;
    }

    /**
     * 设置appSecretId属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppSecretId(String value) {
        this.appSecretId = value;
    }

    /**
     * 获取accessToken属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * 设置accessToken属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccessToken(String value) {
        this.accessToken = value;
    }

    /**
     * 获取eidClass属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEidClass() {
        return eidClass;
    }

    /**
     * 设置eidClass属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEidClass(String value) {
        this.eidClass = value;
    }

}
