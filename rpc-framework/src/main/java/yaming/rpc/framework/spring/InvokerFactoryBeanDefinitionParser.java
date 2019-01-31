package yaming.rpc.framework.spring;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;
import yaming.rpc.framework.invoker.InvokerFactoryBean;

/**
 * Parser of the bean definitions for the Revoker
 */
public class InvokerFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    //logger
    private static final Logger logger = LoggerFactory.getLogger(InvokerFactoryBeanDefinitionParser.class);

    protected Class getBeanClass(Element element) {
        return InvokerFactoryBean.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder bean) {

        try {
            String timeOut = element.getAttribute("timeout");
            String targetInterface = element.getAttribute("interface");
            String clusterStrategy = element.getAttribute("clusterStrategy");
            String remoteAppKey = element.getAttribute("remoteAppKey");
            String groupName = element.getAttribute("groupName");

            bean.addPropertyValue("timeout", Integer.parseInt(timeOut));
            bean.addPropertyValue("targetInterface", Class.forName(targetInterface));
            bean.addPropertyValue("remoteAppKey", remoteAppKey);

            if (StringUtils.isNotBlank(clusterStrategy)) {
                bean.addPropertyValue("clusterStrategy", clusterStrategy);
            }
            if (StringUtils.isNotBlank(groupName)) {
                bean.addPropertyValue("groupName", groupName);
            }
        } catch (Exception e) {
            logger.error("InvokerFactoryBeanDefinitionParser error.", e);
            throw new RuntimeException(e);
        }

    }
}
