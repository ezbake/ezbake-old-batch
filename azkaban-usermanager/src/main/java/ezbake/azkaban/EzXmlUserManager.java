/*   Copyright (C) 2013-2014 Computer Sciences Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

package ezbake.azkaban;

import azkaban.user.User;
import azkaban.user.UserManagerException;
import azkaban.user.XmlUserManager;
import azkaban.utils.Props;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This is an extension on the {@link azkaban.user.XmlUserManager} from Azkaban.  This will make it so that the passwords
 * are not in plain text.  Instead they are SHA256 on disk with the username.
 *
 * A system administrator can just run the linux command:
 * <code>
 *     $ echo -n "jdoe:p@ssw0rd" | sha256sum
 * </code>
 *
 * Where 'jdoe' is the username, and 'p@ssw0rd' is the password for the account.
 */

public class EzXmlUserManager extends XmlUserManager {

    public EzXmlUserManager(Props props) {
        super(props);
    }

    @Override
    public User getUser(String username, String password) throws UserManagerException {
        return super.getUser(username, hashPassword(username, password));
    }

    public static String hashPassword(String username, String password) throws UserManagerException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((username + ":" + password).getBytes());
            byte[] digest = md.digest();
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new UserManagerException("Cant find digest SHA-256 in java.security", e);
        }
    }

    public static String toHex(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
