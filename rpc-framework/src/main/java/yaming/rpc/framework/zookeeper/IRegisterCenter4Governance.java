package yaming.rpc.framework.zookeeper;

import yaming.rpc.framework.model.InvokerService;
import yaming.rpc.framework.model.ProviderService;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface IRegisterCenter4Governance {

    /**
     * retrieve the list of service provider and service consumer
     *
     * @param serviceName
     * @param appKey
     * @return
     */
    public Pair<List<ProviderService>, List<InvokerService>> queryProvidersAndInvokers(String serviceName, String appKey);


}
