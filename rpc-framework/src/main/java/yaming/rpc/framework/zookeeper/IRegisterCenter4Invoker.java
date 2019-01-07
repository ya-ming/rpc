package yaming.rpc.framework.zookeeper;

import yaming.rpc.framework.model.InvokerService;
import yaming.rpc.framework.model.ProviderService;

import java.util.List;
import java.util.Map;

public interface IRegisterCenter4Invoker {
    /**
     * client side. init the local cache of service provider
     */
    public void initProviderMap();

    /**
     * client side. retrieve the information of service provider
     * @return
     */
    public Map<String, List<ProviderService>> getServiceMetaDataMap4Consumer();

    /**
     * client side. register consumer to the node of zookeeper
     * @param invoker
     */
    public void registerInvoker(final InvokerService invoker);
}
