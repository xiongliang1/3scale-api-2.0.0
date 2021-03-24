package com.hisense.gateway.library.utils;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.stud.model.Account;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.LinkedList;
import java.util.List;


/**
 * xml解析工具类
 */
@Slf4j
public class XmlUtils {
    /**
     * xml字符串转对象
     * @param clazz
     * @param xmlStr
     * @return
     */
    public static Object xmlStrToObject(Class clazz, String xmlStr) {
        Object xmlObject = null;
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            // 进行将Xml转成对象的核心接口
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader sr = new StringReader(xmlStr);
            xmlObject = unmarshaller.unmarshal(sr);
        } catch (JAXBException e) {
            log.error("解析异常",e);
        }
        return xmlObject;
    }

    /**
              * 对象转xml字符串
     * @param obj
     * @param load
     * @return
     * @throws JAXBException
     */
    public static String objectToXmlStr(Object obj,Class<?> load){
        String result = "";
        try{
            JAXBContext context = JAXBContext.newInstance(load);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "GBK");
            StringWriter writer = new StringWriter();
            marshaller.marshal(obj,writer);
            result = writer.toString();
        }catch (Exception e){
            log.error("解析异常",e);
        }
        return result;
    }
    
    
    
    public static void main(String [] args) {
//    	String strxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
//    			"<errors>\r\n" + 
//    			"  <error>Users invalid</error>\r\n" + 
//    			"  <error>Email has already been taken</error>\r\n" + 
//    			"  <error>Username has already been taken</error>\r\n" + 
//    			"</errors>";
//    	Object obj = 
//    			XmlUtils.xmlStrToObject(com.tenxcloud.gateway.developer.stud.model.Error.class, strxml);
//    	System.out.print(obj);
//    	
    	
    	
    	String strxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
    			"<account>\r\n" + 
    			"  <id>82</id>\r\n" + 
    			"  <created_at>2019-11-22T07:18:13Z</created_at>\r\n" + 
    			"  <updated_at>2019-11-22T07:18:13Z</updated_at>\r\n" + 
    			"  <state>approved</state>\r\n" + 
    			"  <org_name>test</org_name>\r\n" + 
    			"  <extra_fields></extra_fields>\r\n" + 
    			"  <monthly_billing_enabled>true</monthly_billing_enabled>\r\n" + 
    			"  <monthly_charging_enabled>true</monthly_charging_enabled>\r\n" + 
    			"  <credit_card_stored>false</credit_card_stored>\r\n" + 
    			"  <plans>\r\n" + 
    			"    <plan default=\"true\">\r\n" + 
    			"      <id>106</id>\r\n" + 
    			"      <name>Default</name>\r\n" + 
    			"      <type>account_plan</type>\r\n" + 
    			"      <state>hidden</state>\r\n" + 
    			"      <approval_required>false</approval_required>\r\n" + 
    			"      <setup_fee>0.0</setup_fee>\r\n" + 
    			"      <cost_per_month>0.0</cost_per_month>\r\n" + 
    			"      <trial_period_days/>\r\n" + 
    			"      <cancellation_period>0</cancellation_period>\r\n" + 
    			"    </plan>\r\n" + 
    			"    <plan default=\"true\">\r\n" + 
    			"      <id>105</id>\r\n" + 
    			"      <name>Default</name>\r\n" + 
    			"      <type>service_plan</type>\r\n" + 
    			"      <state>published</state>\r\n" + 
    			"      <approval_required>false</approval_required>\r\n" + 
    			"      <setup_fee>0.0</setup_fee>\r\n" + 
    			"      <cost_per_month>0.0</cost_per_month>\r\n" + 
    			"      <trial_period_days/>\r\n" + 
    			"      <cancellation_period>0</cancellation_period>\r\n" + 
    			"      <service_id>34</service_id>\r\n" + 
    			"    </plan>\r\n" + 
    			"    <plan>\r\n" + 
    			"      <id>122</id>\r\n" + 
    			"      <name>Default</name>\r\n" + 
    			"      <type>service_plan</type>\r\n" + 
    			"      <state>published</state>\r\n" + 
    			"      <approval_required>false</approval_required>\r\n" + 
    			"      <setup_fee>0.0</setup_fee>\r\n" + 
    			"      <cost_per_month>0.0</cost_per_month>\r\n" + 
    			"      <trial_period_days/>\r\n" + 
    			"      <cancellation_period>0</cancellation_period>\r\n" + 
    			"      <service_id>40</service_id>\r\n" + 
    			"    </plan>\r\n" + 
    			"    <plan custom=\"false\" default=\"true\">\r\n" + 
    			"      <id>107</id>\r\n" + 
    			"      <name>Basic</name>\r\n" + 
    			"      <type>application_plan</type>\r\n" + 
    			"      <state>published</state>\r\n" + 
    			"      <approval_required>false</approval_required>\r\n" + 
    			"      <setup_fee>0.0</setup_fee>\r\n" + 
    			"      <cost_per_month>0.0</cost_per_month>\r\n" + 
    			"      <trial_period_days/>\r\n" + 
    			"      <cancellation_period>0</cancellation_period>\r\n" + 
    			"      <service_id>34</service_id>\r\n" + 
    			"      <end_user_required>false</end_user_required>\r\n" + 
    			"    </plan>\r\n" + 
    			"    <plan custom=\"false\" default=\"true\">\r\n" + 
    			"      <id>123</id>\r\n" + 
    			"      <name>planA</name>\r\n" + 
    			"      <type>application_plan</type>\r\n" + 
    			"      <state>published</state>\r\n" + 
    			"      <approval_required>false</approval_required>\r\n" + 
    			"      <setup_fee>0.0</setup_fee>\r\n" + 
    			"      <cost_per_month>0.0</cost_per_month>\r\n" + 
    			"      <trial_period_days/>\r\n" + 
    			"      <cancellation_period>0</cancellation_period>\r\n" + 
    			"      <service_id>40</service_id>\r\n" + 
    			"      <end_user_required>false</end_user_required>\r\n" + 
    			"    </plan>\r\n" + 
    			"  </plans>\r\n" + 
    			"  <users>\r\n" + 
    			"    <user>\r\n" + 
    			"      <id>99</id>\r\n" + 
    			"      <created_at>2019-11-22T07:18:13Z</created_at>\r\n" + 
    			"      <updated_at>2019-11-22T07:18:13Z</updated_at>\r\n" + 
    			"      <account_id>82</account_id>\r\n" + 
    			"      <state>active</state>\r\n" + 
    			"      <role>admin</role>\r\n" + 
    			"      <username>test111</username>\r\n" + 
    			"      <email>test111@qq.com</email>\r\n" + 
    			"      <extra_fields></extra_fields>\r\n" + 
    			"    </user>\r\n" + 
    			"  </users>\r\n" + 
    			"  <applications>\r\n" + 
    			"    <application>\r\n" + 
    			"      <id>365</id>\r\n" + 
    			"      <created_at>2019-11-22T07:18:13Z</created_at>\r\n" + 
    			"      <updated_at>2019-11-22T07:18:13Z</updated_at>\r\n" + 
    			"      <state>live</state>\r\n" + 
    			"      <user_account_id>82</user_account_id>\r\n" + 
    			"      <first_traffic_at/>\r\n" + 
    			"      <first_daily_traffic_at/>\r\n" + 
    			"      <end_user_required>false</end_user_required>\r\n" + 
    			"      <service_id>34</service_id>\r\n" + 
    			"      <user_key>3c8a4c59bdab1f1416d890e30ca63457</user_key>\r\n" + 
    			"      <provider_verification_key>17fe82442c5fb86e7bc1ea8fac675a2b</provider_verification_key>\r\n" + 
    			"      <plan custom=\"false\" default=\"true\">\r\n" + 
    			"        <id>107</id>\r\n" + 
    			"        <name>Basic</name>\r\n" + 
    			"        <type>application_plan</type>\r\n" + 
    			"        <state>published</state>\r\n" + 
    			"        <approval_required>false</approval_required>\r\n" + 
    			"        <setup_fee>0.0</setup_fee>\r\n" + 
    			"        <cost_per_month>0.0</cost_per_month>\r\n" + 
    			"        <trial_period_days/>\r\n" + 
    			"        <cancellation_period>0</cancellation_period>\r\n" + 
    			"        <service_id>34</service_id>\r\n" + 
    			"        <end_user_required>false</end_user_required>\r\n" + 
    			"      </plan>\r\n" + 
    			"      <name>API signup</name>\r\n" + 
    			"      <description>API signup</description>\r\n" + 
    			"      <extra_fields></extra_fields>\r\n" + 
    			"    </application>\r\n" + 
    			"    <application>\r\n" + 
    			"      <id>366</id>\r\n" + 
    			"      <created_at>2019-11-22T07:18:13Z</created_at>\r\n" + 
    			"      <updated_at>2019-11-22T07:18:13Z</updated_at>\r\n" + 
    			"      <state>live</state>\r\n" + 
    			"      <user_account_id>82</user_account_id>\r\n" + 
    			"      <first_traffic_at/>\r\n" + 
    			"      <first_daily_traffic_at/>\r\n" + 
    			"      <end_user_required>false</end_user_required>\r\n" + 
    			"      <service_id>40</service_id>\r\n" + 
    			"      <application_id>8d754b83</application_id>\r\n" + 
    			"      <keys>\r\n" + 
    			"        <key>f8e34f35fbd7763117f4e07e7a87c3c5</key>\r\n" + 
    			"      </keys>\r\n" + 
    			"      <plan custom=\"false\" default=\"true\">\r\n" + 
    			"        <id>123</id>\r\n" + 
    			"        <name>planA</name>\r\n" + 
    			"        <type>application_plan</type>\r\n" + 
    			"        <state>published</state>\r\n" + 
    			"        <approval_required>false</approval_required>\r\n" + 
    			"        <setup_fee>0.0</setup_fee>\r\n" + 
    			"        <cost_per_month>0.0</cost_per_month>\r\n" + 
    			"        <trial_period_days/>\r\n" + 
    			"        <cancellation_period>0</cancellation_period>\r\n" + 
    			"        <service_id>40</service_id>\r\n" + 
    			"        <end_user_required>false</end_user_required>\r\n" + 
    			"      </plan>\r\n" + 
    			"      <name>API signup</name>\r\n" + 
    			"      <description>API signup</description>\r\n" + 
    			"      <extra_fields></extra_fields>\r\n" + 
    			"    </application>\r\n" + 
    			"  </applications>\r\n" + 
    			"</account>";
    	Object obj = 
    			XmlUtils.xmlStrToObject(Account.class, strxml);
    	System.out.print(obj);
    }

	public static JSONObject xml2Json(String xmlStr) throws JDOMException, IOException {
		if (StringUtils.isEmpty(xmlStr)) {
			return null;
		}
		xmlStr = xmlStr.replaceAll("\\\n", "");
		byte[] xml = xmlStr.getBytes("UTF-8");
		JSONObject json = new JSONObject();
		InputStream is = new ByteArrayInputStream(xml);
		SAXBuilder sb = new SAXBuilder();
		Document doc = sb.build(is);
		Element root = doc.getRootElement();
		json.put(root.getName(), iterateElement(root));

		return json;
	}

	private static JSONObject iterateElement(Element element) {
		List<Element> node = element.getChildren();
		JSONObject obj = new JSONObject();
		List list = null;
		for (Element child : node) {
			list = new LinkedList();
			String text = child.getTextTrim();
			if (StringUtils.isBlank(text)) {
				if (child.getChildren().size() == 0) {
					continue;
				}
				if (obj.containsKey(child.getName())) {
					list = (List) obj.get(child.getName());
				}
				list.add(iterateElement(child)); //遍历child的子节点
				obj.put(child.getName(), list);
			} else {
				if (obj.containsKey(child.getName())) {
					Object value = obj.get(child.getName());
					try {
						list = (List) value;
					} catch (ClassCastException e) {
						list.add(value);
					}
				}
				if (child.getChildren().size() == 0) { //child无子节点时直接设置text
					obj.put(child.getName(), text);
				} else {
					list.add(text);
					obj.put(child.getName(), list);
				}
			}
		}
		return obj;
	}
}