package parser.tbh;

import lombok.Data;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;

public class Main {

    public static void main(String[] args) throws DecoderException {
        FullStackEncoder parser = new FullStackEncoder();

        parser.read(Hex.decodeHex("aaaaaaaa7e7e5d5e112233445566778800040005"));
        parser.read(Hex.decodeHex("012233445566778899"));
        parser.read(Hex.decodeHex("112233445566778899"));
        parser.read(Hex.decodeHex("212233445566778899"));
        parser.read(Hex.decodeHex("312233445566778899"));
        parser.read(Hex.decodeHex("4122334455667788997e7e5d5e"));
        parser.read(Hex.decodeHex("aaaaaaaa7e7e5d5e112233445566778800040005"));


        System.out.println("Dump: " + Hex.encodeHexString(parser.dump()));


    }


    static class CircularBuffer {
        private int capacity;
        private byte[] buffer;

        private int readerIndex;
        private int writerIndex;
        private int size;

        public CircularBuffer(int capacity) {
            this.capacity = capacity;
            buffer = new byte[capacity];
        }

        public int write(byte[] bytes, int offset, int length) {
            if (length == 0) return length;

            int writeLen = Math.min(capacity - size, length);
            int clippedLen = clip(writerIndex + writeLen);

            if (clippedLen > writerIndex || clippedLen == 0) {
                System.arraycopy(bytes, offset, buffer, writerIndex, writeLen);
            } else {
                System.arraycopy(bytes, offset, buffer, writerIndex, writeLen - clippedLen);
                System.arraycopy(bytes, offset + writeLen - clippedLen, buffer, 0, clippedLen);
            }

            writerIndex = clippedLen;
            size += writeLen;

            return writeLen;
        }

        public int size() {
            return size;
        }

        public byte[] read(int n) {
            int len = Math.min(size, n);
            byte[] copied = new byte[len];
            if (readerIndex + len < capacity) {
                System.arraycopy(buffer, readerIndex, copied, 0, len);
            } else {
                System.arraycopy(buffer, readerIndex, copied, 0, capacity - readerIndex);
                System.arraycopy(buffer, 0, copied, capacity - readerIndex, len - capacity + readerIndex);

            }
            return copied;
        }

        public byte get() {
            return get(0);
        }

        public byte get(int index) {
            if (index < size) {
                return buffer[clip(readerIndex + index)];
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public int find(byte b) {
            for (int i = 0; i < size; i++) {
                if (get(i) == b) {
                    return i;
                }
            }
            return -1;
        }


        public int skip() {
            return skip(1);
        }

        public int skip(int n) {
            int skipLen = Math.min(n, size);
            size -= skipLen;
            readerIndex = clip(readerIndex + n);
            return skipLen;
        }

        private int clip(int n) {
            return n % capacity;

        }
    }

    @Data
    static class Packet {
        short length;
        short crc;

        public Packet(byte[] bytes) {
            parse(bytes);
        }

        private void parse(byte[] bytes) {
            length = ByteBuffer.wrap(bytes, 12, 2).getShort();
            crc = ByteBuffer.wrap(bytes, 14, 2).getShort();
        }

        public boolean isValid() {
            return crc - length == 1;
        }
    }

    static class FullStackEncoder {


        private enum STATE {
            SEARCHING_FOR_SYN, SEARCHING_FOR_TBH, SEARCHING_FOR_PACKET
        }

        static final byte SYN = 0x7e;
        private STATE state;
        private int requiredBytes;
        private Packet packet;
        private CircularBuffer buffer;

        public FullStackEncoder() {
            this(1500);
        }

        public FullStackEncoder(int maxPacketSize) {
            buffer = new CircularBuffer(maxPacketSize);
            reset();

        }

        public byte[] dump() {
            return buffer.read(buffer.size);
        }

        public void reset() {
            state = STATE.SEARCHING_FOR_SYN;
            buffer.skip(buffer.size());
            packet = null;
            requiredBytes = 16;
        }

        public void read(byte[] bytes) {
            int remainingBytes = bytes.length - buffer.write(bytes, 0, bytes.length);
            while (buffer.size >= requiredBytes) {
                remainingBytes -= buffer.write(bytes, bytes.length - remainingBytes, remainingBytes);
                switch (state) {
                    case SEARCHING_FOR_SYN:
                        searchingForSyn();
                        break;
                    case SEARCHING_FOR_TBH:
                        searchingForTbh();
                        break;
                    case SEARCHING_FOR_PACKET:
                        searchingForPacket();
                        break;
                }
            }
            System.out.println(state);

        }

        private void searchingForPacket() {
            if (buffer.size() >= requiredBytes) {
                byte[] readBytes = buffer.read(requiredBytes);
                System.out.println(Hex.encodeHexString(readBytes));
                buffer.skip(requiredBytes);
                state = STATE.SEARCHING_FOR_SYN;
                requiredBytes = 16;
            }
        }

        private void searchingForTbh() {
            if (buffer.size() >= requiredBytes) {
                byte[] readBytes = buffer.read(requiredBytes);
                packet = new Packet(readBytes);
                if (packet.isValid()) {
                    state = STATE.SEARCHING_FOR_PACKET;
                    requiredBytes = 9 * packet.getLength() + 16;
                } else {
                    state = STATE.SEARCHING_FOR_SYN;
                    buffer.skip(4);
                }
            }
        }

        private void searchingForSyn() {
            if (buffer.size() >= requiredBytes) {
                if (buffer.get() == SYN && buffer.get(1) == SYN && buffer.get(2) == 0x5d && buffer.get(3) == 0x5e) {
                    state = STATE.SEARCHING_FOR_TBH;
                } else {
                    buffer.skip();
                    int index = buffer.find(SYN);
                    if (index > -1) {
                        buffer.skip(index);
                    }
                }
            }
        }
    }

}
