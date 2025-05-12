/*
 * DavMail POP/IMAP/SMTP/CalDav/LDAP Exchange Gateway
 * Copyright (C) 2010  Mickael Guessant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package davmail.http;

import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;
import org.apache.http.impl.auth.NTLMEngine;
import org.apache.http.impl.auth.NTLMEngineException;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * JCIFS based NTLM authentication.
 */
public final class JCIFSEngine implements NTLMEngine {
    static final Logger LOGGER = Logger.getLogger("davmail.http.JCIFSEngine");

    private static final int TYPE_1_FLAGS =
            NtlmFlags.NTLMSSP_NEGOTIATE_NTLM2 | NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN |
                    NtlmFlags.NTLMSSP_NEGOTIATE_OEM_WORKSTATION_SUPPLIED | NtlmFlags.NTLMSSP_NEGOTIATE_OEM_DOMAIN_SUPPLIED |
                    NtlmFlags.NTLMSSP_NEGOTIATE_NTLM | NtlmFlags.NTLMSSP_REQUEST_TARGET |
                    NtlmFlags.NTLMSSP_NEGOTIATE_OEM | NtlmFlags.NTLMSSP_NEGOTIATE_UNICODE |
                    NtlmFlags.NTLMSSP_NEGOTIATE_56 | NtlmFlags.NTLMSSP_NEGOTIATE_128;

    public String generateType1Msg(final String domain, final String workstation) {
        final Type1Message type1Message = new Type1Message(TYPE_1_FLAGS, domain, workstation);
        LOGGER.debug("Generate Type1Msg "+type1Message);
        String encodedType1Message = Base64.encode(type1Message.toByteArray());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(NTLMMessageDecoder.decode(encodedType1Message));
        }
        return encodedType1Message;
    }

    public String generateType3Msg(final String username, final String password,
                                   final String domain, final String workstation, final String challenge)
            throws NTLMEngineException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(NTLMMessageDecoder.decode(challenge));
        }

        Type2Message type2Message;
        try {
            type2Message = new Type2Message(Base64.decode(challenge));
        } catch (final IOException exception) {
            throw new NTLMEngineException("Invalid NTLM type 2 message", exception);
        }
        LOGGER.debug("Received Type2Msg " + type2Message);

        // from HttpClient 4 doc
        // final int type3Flags = type2Message.getFlags()
        //        & (0xffffffff ^ (NtlmFlags.NTLMSSP_TARGET_TYPE_DOMAIN | NtlmFlags.NTLMSSP_TARGET_TYPE_SERVER));
        int type3Flags = NtlmFlags.NTLMSSP_NEGOTIATE_NTLM2 | NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN |
                NtlmFlags.NTLMSSP_NEGOTIATE_NTLM | NtlmFlags.NTLMSSP_NEGOTIATE_UNICODE;
        Type3Message type3Message = new Type3Message(type2Message, password,
                domain, username, workstation, type3Flags);
        LOGGER.debug("Generate Type3Msg "+type3Message);
        String encodedType3Message = Base64.encode(type3Message.toByteArray());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(NTLMMessageDecoder.decode(encodedType3Message));
        }
        return encodedType3Message;
    }

}