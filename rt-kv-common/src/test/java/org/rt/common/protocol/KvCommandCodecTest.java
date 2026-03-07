package org.rt.common.protocol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * {@link KvCommandCodec} 的单元测试。
 */
class KvCommandCodecTest {

    /**
     * 验证 PUT 命令的编解码往返一致性。
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
     * 验证非法载荷会被拒绝。
     */
    @Test
    void shouldRejectInvalidPayload() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> KvCommandCodec.decode("PUT\ta"));
    }
}