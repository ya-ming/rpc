package yaming.rpc.framework.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class RpcRemoteReferenceNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("reference", new InvokerFactoryBeanDefinitionParser());
    }
}
