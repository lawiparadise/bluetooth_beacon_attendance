package com.law.blueinnofora;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by gd2 on 2015-07-02.
 * 비콘 아이덴티파이어를 임의의 바이트 렝스로 엔캡슐한다.
 */
public class Identifier implements Comparable<Identifier> {
    //?
    private static final Pattern HEX_PATTERN = Pattern.compile("^0x[0-9A-Fa-f]*$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^[0-9]{1,5}$");

    // BUG: Dashes in UUIDs are not optional!
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9A-Fa-f]{8}-?[0-9A-Fa-f]{4}-?[0-9A-Fa-f]{4}-?[0-9A-Fa-f]{4}-?[0-9A-Fa-f]{12}$");
    private static final int MAX_INTEGER = 65535;

    private final byte[] mValue;

    public static Identifier parse(String stringValue){
        if(stringValue==null){
            throw new NullPointerException("아이덴티파이어가 생성되지 않았다");
        }

        if(HEX_PATTERN.matcher(stringValue).matches()){
            return parseHex(stringValue.substring(2));
        }
        if (UUID_PATTERN.matcher(stringValue).matches()) {
            return parseHex(stringValue.replace("-", ""));
        }

        if (DECIMAL_PATTERN.matcher(stringValue).matches()) {
            int value = -1;
            try {
                value = Integer.valueOf(stringValue);
            }
            catch (Throwable t) {
                throw new IllegalArgumentException("Unable to parse Identifier in decimal format.", t);
            }
            return fromInt(value);
        }

        throw new IllegalArgumentException("Unable to parse Identifier.");
    }

    private static Identifier parseHex(String identifierString) {
        String str = identifierString.length() % 2 == 0 ? "" : "0";
        str += identifierString.toUpperCase();
        byte[] result = new byte[str.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte)(Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16) & 0xFF);
        }
        return new Identifier(result);
    }
    public static Identifier fromInt(int intValue) {
        if (intValue < 0 || intValue > MAX_INTEGER) {
            throw new IllegalArgumentException("Identifers can only be constructed from integers between 0 and " + MAX_INTEGER + " (inclusive).");
        }

        byte[] newValue = new byte[2];

        newValue[0] = (byte) (intValue >> 8);
        newValue[1] = (byte) (intValue);

        return new Identifier(newValue);
    }

    public static Identifier fromBytes(byte[] bytes, int start, int end, boolean littleEndian) {
        if (bytes == null) {
            throw new NullPointerException("Identifiers cannot be constructed from null pointers but \"bytes\" is null.");
        }
        if (start < 0 || start > bytes.length) {
            throw new ArrayIndexOutOfBoundsException("start < 0 || start > bytes.length");
        }
        if (end > bytes.length) {
            throw new ArrayIndexOutOfBoundsException("end > bytes.length");
        }
        if (start > end) {
            throw new IllegalArgumentException("start > end");
        }

        byte[] byteRange = Arrays.copyOfRange(bytes, start, end);
        if (littleEndian) {
            reverseArray(byteRange);
        }
        return new Identifier(byteRange);
    }
    public static Identifier fromUuid(UUID uuid) {
        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.putLong(uuid.getMostSignificantBits());
        buf.putLong(uuid.getLeastSignificantBits());
        return new Identifier(buf.array());
    }
    public Identifier(Identifier identifier) {
        if (identifier == null) {
            throw new NullPointerException("Identifiers cannot be constructed from null pointers but \"identifier\" is null.");
        }
        mValue = identifier.mValue;
    }
    protected Identifier(byte[] value) {
        if (value == null) {
            throw new NullPointerException("Identifiers cannot be constructed from null pointers but \"value\" is null.");
        }
        this.mValue = value;
    }
    public String toString() {
        // Note:  the toString() method is also used for serialization and deserialization.  So
        // toString() and parse() must always return objects that return true when you call equals()
        if (mValue.length == 2) {
            return Integer.toString(toInt());
        }
        if (mValue.length == 16) {
            return toUuid().toString();
        }
        return toHexString();
    }
    public int toInt() {
        if (mValue.length > 2) {
            throw new UnsupportedOperationException("Only supported for Identifiers with max byte length of 2");
        }
        int result = 0;

        for (int i = 0; i < mValue.length; i++) {
            result |= (mValue[i] & 0xFF) << ((mValue.length - i - 1) * 8);
        }

        return result;
    }
    public byte[] toByteArrayOfSpecifiedEndianness(boolean bigEndian) {
        byte[] copy = Arrays.copyOf(mValue, mValue.length);

        if (!bigEndian) {
            reverseArray(copy);
        }

        return copy;
    }

    private static void reverseArray(byte[] bytes) {
        for (int i = 0; i < bytes.length / 2; i++) {
            int mirroredIndex = bytes.length - i - 1;
            byte tmp = bytes[i];
            bytes[i] = bytes[mirroredIndex];
            bytes[mirroredIndex] = tmp;
        }
    }

    public int getByteCount() {
        return mValue.length;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mValue);
    }

    /**
     * Returns whether both Identifiers contain equal value. This is the case when the value is the same
     * and has the same length
     * @param that object to compare to
     * @return whether that equals this
     */
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof Identifier)) {
            return false;
        }
        Identifier thatIdentifier = (Identifier) that;
        return Arrays.equals(mValue, thatIdentifier.mValue);
    }

    /**
     * Represents the value as a hexadecimal String. The String is prefixed with <code>0x</code>. For example 0x0034ab
     * @return value as hexadecimal String
     */
    public String toHexString() {
        StringBuilder sb = new StringBuilder(2 + 2 * mValue.length);
        sb.append("0x");
        for (byte item : mValue) {
            sb.append(String.format("%02x", item));
        }
        return sb.toString();
    }

    /**
     * Returns the value of this Identifier in UUID format. For example 2f234454-cf6d-4a0f-adf2-f4911ba9ffa6
     * @deprecated Replaced by stronger typed variant.
     *    This mathod returns a string, therefore does not offer type safety on
     *    the UUID per se. It was replaced by {@link #toUuid()}.
     * @return value in UUID format
     * @throws UnsupportedOperationException when value length is not 16 bytes
     */
    @Deprecated
    public String toUuidString() {
        return toUuid().toString();
    }

    /**
     * Gives you the Identifier as a UUID if possible.
     *
     * @throws UnsupportedOperationException if the byte array backing this Identifier is not exactly
     *         16 bytes long.
     */
    public UUID toUuid() {
        if (mValue.length != 16) {
            throw new UnsupportedOperationException("Only Identifiers backed by a byte array with length of exactly 16 can be UUIDs.");
        }
        LongBuffer buf = ByteBuffer.wrap(mValue).asLongBuffer();
        return new UUID(buf.get(), buf.get());
    }
    public byte[] toByteArray() {
        return mValue.clone();
    }

    @Override
    public int compareTo(Identifier that) {
        if (mValue.length != that.mValue.length) {
            return mValue.length < that.mValue.length ? -1 : 1;
        }
        for (int i = 0; i < mValue.length; i++) {
            if (mValue[i] != that.mValue[i]) {
                return mValue[i] < that.mValue[i] ? -1 : 1;
            }
        }
        return 0;
    }

}
