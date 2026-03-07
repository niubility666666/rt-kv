package org.rt.common.protocol;

import java.util.UUID;

/**
 * Shared constants used by all rt-kv modules.
 *
 * <p>The Raft group identifier must stay consistent across client and server.
 */
public final class RaftGroupConstants {

    /**
     * Stable Raft group UUID for this kv cluster.
     */
    public static final UUID KV_GROUP_UUID = UUID.fromString("12345678-1234-1234-1234-123456789012");

    private RaftGroupConstants() {
        // Utility class.
    }
}
