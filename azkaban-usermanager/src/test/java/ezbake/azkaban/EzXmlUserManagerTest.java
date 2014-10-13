package ezbake.azkaban;

import azkaban.user.XmlUserManager;
import azkaban.utils.Props;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EzXmlUserManagerTest {
    private Props baseProps = new Props();

    @Test
    public void testGetFoundUser() throws Exception {
        Props props = new Props(baseProps);
        props.put(XmlUserManager.XML_FILE_PARAM,
                "src/test/resources/test-conf/azkaban-users-test1.xml");

        XmlUserManager userManager = new EzXmlUserManager(props);
        assertNotNull(userManager.getUser("jdoe", "p@ssw0rd"));
    }

    @Test(expected=azkaban.user.UserManagerException.class)
    public void testGetFoundUserBadPassword() throws Exception {
        Props props = new Props(baseProps);
        props.put(XmlUserManager.XML_FILE_PARAM,
                "src/test/resources/test-conf/azkaban-users-test1.xml");

        XmlUserManager userManager = new EzXmlUserManager(props);
        userManager.getUser("jdoe", "NotMyPassword");
    }

    @Test
    public void testHashPassword() throws Exception {
        // this came from: $ echo -n "jdoe:p@ssw0rd" | sha256sum
        final String expected = "0590c4dd22dd042d84689c0ffe80e948206a1a576bd48939914185feebcc62dc";
        assertEquals(expected, EzXmlUserManager.hashPassword("jdoe", "p@ssw0rd"));
    }

    @Test
    public void testToHex() throws Exception {
        final String expected = Integer.toHexString(312343356);
        final byte[] byteArray = ByteBuffer.allocate(4).putInt(312343356).array();
        assertEquals(expected, EzXmlUserManager.toHex(byteArray));
    }
}