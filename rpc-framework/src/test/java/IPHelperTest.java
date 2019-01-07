import static yaming.rpc.framework.helper.IPHelper.getHostFirstIp;
import static yaming.rpc.framework.helper.IPHelper.getRealIp;

public class IPHelperTest {
    public static void main(String[] args) throws Exception {
        //System.out.println(localIp());
        System.out.println(getRealIp());
        System.out.println(getHostFirstIp());
    }
}
