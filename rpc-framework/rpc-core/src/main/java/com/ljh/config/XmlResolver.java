package com.ljh.config;

import com.ljh.ProtocolConfig;
import com.ljh.compress.Compressor;
import com.ljh.compress.CompressorFactory;
import com.ljh.discovery.RegistryConfig;
import com.ljh.loadbalancer.LoadBalancer;
import com.ljh.serialize.Serializer;
import com.ljh.serialize.SerializerFactory;
import com.ljh.untils.id.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * @author ljh
 *
 */

public class XmlResolver {

    private static final Logger log = LoggerFactory.getLogger(XmlResolver.class);


    public void loadFromXml(Configuration configuration) {
        try {
            // 1、创建一个document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 禁用DTD校验：可以通过调用setValidating(false)方法来禁用DTD校验。
            factory.setValidating(false);
            // 禁用外部实体解析：可以通过调用setFeature(String name, boolean value)方法并将“http://apache.org/xml/features/nonvalidating/load-external-dtd”设置为“false”来禁用外部实体解析。
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("rpc.xml");
            Document doc = builder.parse(inputStream);

            // 2、获取一个xpath解析器
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            // 3、解析所有的标签
            configuration.setPort(resolvePort(doc, xpath));
            configuration.setAppName(resolveAppName(doc, xpath));

            configuration.setIdGenerator(resolveIdGenerator(doc, xpath));

            configuration.setRegistryConfig(resolveRegistryConfig(doc, xpath));


            // 处理使用的压缩方式和序列化方式
            configuration.setCompressType(resolveCompressType(doc, xpath));
            configuration.setSerializeType(resolveSerializeType(doc, xpath));


            ObjectWrapper<Compressor> compressorObjectWrapper = resolveCompressCompressor(doc, xpath);
            CompressorFactory.addCompressor(compressorObjectWrapper);

            ObjectWrapper<Serializer> serializerObjectWrapper = resolveSerializer(doc, xpath);
            SerializerFactory.addSerializer(serializerObjectWrapper);

            configuration.setLoadBalancer(resolveLoadBalancer(doc, xpath));


        } catch (SAXException | ParserConfigurationException | IOException e) {
            log.info("If no configuration file is found or an exception occurs when parsing the configuration file, " +
                    "the default configuration is used.", e);
        }




    }

    /**
     * 解析序列化器
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 序列化器
     */
    private ObjectWrapper<Serializer> resolveSerializer(Document doc, XPath xpath) {
        String expression = "/configuration/serializer";
        Serializer serializer = parseObject(doc, xpath, expression, null);
        Byte code = Byte.valueOf(Objects.requireNonNull(parseString(doc, xpath, expression, "code")));
        String name = parseString(doc, xpath, expression, "name");
        return new ObjectWrapper<>(code,name,serializer);
    }



    /**
     * 解析序列化的方式
     * @param doc
     * @param xpath xpath解析器
     * @return
     */
    private String resolveSerializeType(Document doc, XPath xpath) {

        String expression = "/configuration/serializeType";
        return parseString(doc, xpath, expression, "type");

    }



    /**
     * 解析压缩的具体实现
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return ObjectWrapper<Compressor>
     */
    private ObjectWrapper<Compressor> resolveCompressCompressor(Document doc, XPath xpath) {
        String expression = "/configuration/compressor";
        Compressor compressor = parseObject(doc, xpath, expression, null);
        Byte code = Byte.valueOf(Objects.requireNonNull(parseString(doc, xpath, expression, "code")));
        String name = parseString(doc, xpath, expression, "name");
        return new ObjectWrapper<>(code,name,compressor);
    }


    /**
     * 解析压缩的算法名称
     * @param doc
     * @param xpath xpath解析器
     * @return
     */
    private String resolveCompressType(Document doc, XPath xpath) {
        String expression = "/configuration/compressType";
        return parseString(doc, xpath, expression, "type");

    }

    /**
     * 解析负载均衡器
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 负载均衡器实例
     */
    private LoadBalancer resolveLoadBalancer(Document doc, XPath xpath) {
        String expression = "/configuration/loadBalancer";
        return parseObject(doc, xpath, expression, null);
    }



    /**
     * 解析端口
     * @param doc
     * @param xpath
     * @return
     */
    private int resolvePort(Document doc, XPath xpath) {

        String parseString = null;
        String expression = "/configuration/port";
        parseString = parseString(doc, xpath, expression);


        return Integer.parseInt(parseString);

    }




    /**
     * 解析应用名
     * @param doc
     * @param xpath
     * @return
     */
    private String resolveAppName(Document doc, XPath xpath) {

        String expression = "/configuration/appName";
        return parseString(doc, xpath, expression);
    }


    /**
     * 解析id发号器
     *
     * @param doc
     * @param xpath
     * @return
     */
    private IdGenerator resolveIdGenerator(Document doc, XPath xpath) {
        String expression = "/configuration/idGenerator";
        String aClass = parseString(doc, xpath, expression, "class");
        String dataCenterId = parseString(doc, xpath, expression, "dataCenterId");
        String machineId = parseString(doc, xpath, expression, "MachineId");

        try {
            Class<?> clazz = Class.forName(aClass);
            Object instance = clazz.getConstructor(new Class[]{long.class, long.class})
                    .newInstance(Long.parseLong(dataCenterId), Long.parseLong(machineId));
            return (IdGenerator) instance;
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * 解析注册中心
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return RegistryConfig
     */
    private RegistryConfig resolveRegistryConfig(Document doc, XPath xpath) {
        String expression = "/configuration/registry";
        String url = parseString(doc, xpath, expression, "url");
        return new RegistryConfig(url);


    }



    /**
     * 获得一个节点文本值
     * @param doc        文档对象
     * @param xpath      xpath解析器
     * @param expression xpath表达式
     * @return 节点的值
     */
    private String parseString(Document doc, XPath xpath, String expression) {
        try {
            String textContent = null;
            XPathExpression expr = xpath.compile(expression);
            // 我们的表达式可以帮我们获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            textContent = targetNode.getTextContent();
            return textContent;
        } catch (XPathExpressionException e) {
            log.error("An exception occurred while parsing the expression.", e);
        }
        return null;
    }


    /**
     * 获得一个节点属性的值
     * @param doc           文档对象
     * @param xpath         xpath解析器
     * @param expression    xpath表达式
     * @param AttributeName 节点名称
     * @return 节点的值
     */
    private String parseString(Document doc, XPath xpath, String expression, String AttributeName) {
        try {
            XPathExpression expr = xpath.compile(expression);
            // 我们的表达式可以帮我们获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getAttributes().getNamedItem(AttributeName).getNodeValue();
        } catch (XPathExpressionException e) {
            log.error("An exception occurred while parsing the expression.", e);
        }
        return null;
    }


    /**
     * 解析一个节点，返回一个实例
     * @param doc        文档对象
     * @param xpath      xpath解析器
     * @param expression xpath表达式
     * @param paramType  参数列表
     * @param param      参数
     * @param <T>        泛型
     * @return 配置的实例
     */
    private <T> T parseObject(Document doc, XPath xpath, String expression, Class<?>[] paramType, Object... param) {
        try {
            XPathExpression expr = xpath.compile(expression);
            // 我们的表达式可以帮我们获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            String className = targetNode.getAttributes().getNamedItem("class").getNodeValue();
            Class<?> aClass = Class.forName(className);
            Object instant = null;
            if (paramType == null) {
                instant = aClass.getConstructor().newInstance();
            } else {
                instant = aClass.getConstructor(paramType).newInstance(param);
            }
            return (T) instant;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                InvocationTargetException | XPathExpressionException e) {
            log.error("An exception occurred while parsing the expression.", e);
        }
        return null;
    }



}
