package yaming.rpc.framework.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RpcResponseWrapper {

    // blocking queue to store the responses
    private BlockingQueue<RpcResponse> responseQueue = new ArrayBlockingQueue<RpcResponse>(1);
    // timestamp of the response
    private long responseTime;

    /**
     * check whether the result expired or not
     *
     * @return
     */
    public boolean isExpire() {
        RpcResponse response = responseQueue.peek();
        if (response == null) {
            return false;
        }

        long timeout = response.getInvokeTimeout();
        if ((System.currentTimeMillis() - responseTime) > timeout) {
            return true;
        }
        return false;
    }

    public static RpcResponseWrapper of() {
        return new RpcResponseWrapper();
    }

    public BlockingQueue<RpcResponse> getResponseQueue() {
        return responseQueue;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
}
