package yaming.rpc.framework.invoker;

import com.google.common.collect.Maps;
import yaming.rpc.framework.model.RpcResponse;
import yaming.rpc.framework.model.RpcResponseWrapper;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class InvokerResponseHolder {
    // map to store the response returned by the service
    private static final Map<String, RpcResponseWrapper> responseMap = Maps.newConcurrentMap();
    // executor for cleaning up the expired response
    private static final ExecutorService removeExpireKeyExecutor = Executors.newSingleThreadExecutor();

    static {
        // delete the expired response to prevent memory leak
        removeExpireKeyExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        for (Map.Entry<String, RpcResponseWrapper> entry : responseMap.entrySet()) {
                            boolean isExpire = entry.getValue().isExpire();
                            if (isExpire) {
                                responseMap.remove(entry.getKey());
                            }
                            Thread.sleep(10);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    /**
     * init the container
     *
     * @param requestUniqueKey
     */
    public static void initResponseData(String requestUniqueKey) {
        responseMap.put(requestUniqueKey, RpcResponseWrapper.of());
    }


    /**
     * Store the async-response from Netty channel into blocking queue
     *
     * @param response
     */
    public static void putResultValue(RpcResponse response) {
        long currentTime = System.currentTimeMillis();
        RpcResponseWrapper responseWrapper = responseMap.get(response.getUniqueKey());
        responseWrapper.setResponseTime(currentTime);
        responseWrapper.getResponseQueue().add(response);
        responseMap.put(response.getUniqueKey(), responseWrapper);
    }


    /**
     * Retrieve the async-response from the blocking queue
     *
     * @param requestUniqueKey
     * @param timeout
     * @return
     */
    public static RpcResponse getValue(String requestUniqueKey, long timeout) {
        RpcResponseWrapper responseWrapper = responseMap.get(requestUniqueKey);
        try {
            return responseWrapper.getResponseQueue().poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            responseMap.remove(requestUniqueKey);
        }
    }
}
