package ru.hobbut.hudson.utils;

import junit.framework.TestCase;
import ru.hobbut.hudson.model.Host;

/**
 * Created by IntelliJ IDEA.
 * User: hobbut
 * Date: 1/14/11
 * Time: 4:57 PM
 */
public class UtilsTest extends TestCase {
    public void testGetConnectInfo() throws Exception {
        ConnectInfo info = new ConnectInfo();
        info.setHost("10.0.1.4");
        info.setPort(10022);
        info.setProtocol(ConnectInfo.ProtocolType.SCP);
        info.setPath("/home/staff/hudson");
        info.setUsername("a");
        info.setPassword("b");

        ConnectInfo connectInfo = Utils.getConnectInfo(new Host("scp://10.0.1.4:10022/home/staff/hudson", "a", "b"));

        System.out.println(info);
        System.out.println(connectInfo);
        assertEquals(info, connectInfo);
    }
}
