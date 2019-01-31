package yaming.rpc.framework.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class RpcRemoteServiceNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("service", new ProviderFactoryBeanDefinitionParser());
    }
}
