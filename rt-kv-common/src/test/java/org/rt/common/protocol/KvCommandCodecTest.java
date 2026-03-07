package org.rt.common.protocol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link KvCommandCodec}.
 */
class KvCommandCodecTest {

    /**
     * Verifies round trip for put command.
     */
    @Test
    void shouldEncodeAndDecodePut() {
        KvCommand put = KvCommand.put("k with space", "v with symbols !@#");
        String encoded = KvCommandCodec.encode(put);

        KvCommand decoded = KvCommandCodec.decode(encoded);

        Assertions.assertEquals(KvCommandType.PUT, decoded.type());
        Assertions.assertEquals("k with space", decoded.key());
        Assertions.assertEquals("v with symbols !@#", decoded.value());
    }

    /**
     * Verifies invalid payload is rejected.
     */
    @Test
    void shouldRejectInvalidPayload() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> KvCommandCodec.decode("PUT\ta"));
    }
}
