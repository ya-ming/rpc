package yaming.rpc.framework.zookeeper;

import yaming.rpc.framework.model.ProviderService;

import java.util.List;
import java.util.Map;

public interface IRegisterCenter4Provider {
    /**
     * server side. register service provider to the zookeeper node
     * @param serviceMetaData
     */
    public void registerProvider(final List<ProviderService> serviceMetaData);

    /**
     * server side. retrieve the information of service provider
     * key: interface of the service provider
     * value: method list of the service provider
     * @return
     */
    public Map<String, List<ProviderService>> getProviderServiceMap();
}
