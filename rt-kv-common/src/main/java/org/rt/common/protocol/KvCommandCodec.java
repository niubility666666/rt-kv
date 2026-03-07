package org.rt.common.protocol;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Encodes and decodes {@link KvCommand} into a compact text protocol.
 *
 * <p>Format:
 * <ul>
 *     <li>PUT\tbase64(key)\tbase64(value)</li>
 *     <li>GET\tbase64(key)</li>
 *     <li>DELETE\tbase64(key)</li>
 * </ul>
 */
public final class KvCommandCodec {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final String DELIMITER = "\t";

    private KvCommandCodec() {
        // Utility class.
    }

    /**
     * Encodes a command to protocol text.
     *
     * @param command command object
     * @return serialized command
     */
    public static String encode(KvCommand command) {
        return switch (command.type()) {
            case PUT -> command.type().name() + DELIMITER + encodePart(command.key()) + DELIMITER + encodePart(command.value());
            case GET, DELETE -> command.type().name() + DELIMITER + encodePart(command.key());
        };
    }

    /**
     * Decodes protocol text into command object.
     *
     * @param payload serialized command
     * @return command object
     */
    public static KvCommand decode(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("payload must not be blank");
        }

        String[] parts = payload.split(DELIMITER, -1);
        KvCommandType type = KvCommandType.valueOf(parts[0]);

        return switch (type) {
            case PUT -> {
                validateLength(parts, 3, type);
                yield KvCommand.put(decodePart(parts[1]), decodePart(parts[2]));
            }
            case GET -> {
                validateLength(parts, 2, type);
                yield KvCommand.get(decodePart(parts[1]));
            }
            case DELETE -> {
                validateLength(parts, 2, type);
                yield KvCommand.delete(decodePart(parts[1]));
            }
        };
    }

    private static String encodePart(String raw) {
        return ENCODER.encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodePart(String encoded) {
        return new String(DECODER.decode(encoded), StandardCharsets.UTF_8);
    }

    private static void validateLength(String[] parts, int expected, KvCommandType type) {
        if (parts.length != expected) {
            throw new IllegalArgumentException("Invalid payload length for " + type + ": " + parts.length);
        }
    }
}
