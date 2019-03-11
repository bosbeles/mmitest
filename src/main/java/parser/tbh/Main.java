package parser.tbh;

import lombok.Data;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static int writerCounter;
    static int foundCounter;
    static int callCounter;

    static boolean log = false;
    static int numberOfTries = 3;
    static int testSize = 1000000;

    public static void main(String[] args) throws DecoderException, InterruptedException {



        List<byte[]> bytes = new ArrayList<>();
        bytes.add(Hex.decodeHex("01aaaaaa7e7e5d5e112233445566778800240025"));
        bytes.add(Hex.decodeHex("022233445566778899"));
        bytes.add(Hex.decodeHex("0322334455667788997e7e5d5e"));
        bytes.add(Hex.decodeHex("042233445566778899"));
        bytes.add(Hex.decodeHex("052233445566778899"));
        bytes.add(Hex.decodeHex("0522334455667788990522334455667788990522334455667788990522334455667788997e0522334455667788990522334455667788990522334455667788997e"));
        bytes.add(Hex.decodeHex("0632323232323232323232323232327e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e7e3232323232323232323232323232323232323232323232323232323232323232323232323232327e7e5d5e"));
        bytes.add(Hex.decodeHex("072233447e7e5d5e66778800120013"));
        bytes.add(Hex.decodeHex("0800020003447e7e5d5e7e7e7e5d5e"));
        bytes.add(Hex.decodeHex("09aaaaaa7e7e5d5e112233445566778800120013"));
        bytes.add(Hex.decodeHex("102233445566778899"));
        bytes.add(Hex.decodeHex("112233445566778899"));
        bytes.add(Hex.decodeHex("7e7e5d5e112233445566778800120013"));
        bytes.add(Hex.decodeHex("012233445566778899"));
        bytes.add(Hex.decodeHex("112233445566778800"));
        bytes.add(Hex.decodeHex("1122334455667788997e007e7e5d5e7e7e5d5e7e7e5d5e"));
        bytes.add(Hex.decodeHex("7e7e000000120013112233445566778800120013"));
        bytes.add(Hex.decodeHex("7e7e5d5e112233445566778800120013"));
        bytes.add(Hex.decodeHex("012233445566778899"));
        bytes.add(Hex.decodeHex("112233445566778899"));
        bytes.add(Hex.decodeHex("7e7e5d5e1122334455667788001200130122334455667788990122334455667788997e7e5d5e1122334455667788001200130122334455667788990122334455667788997e7e5d5e1122334455667788001200130122334455667788990122334455667788997e7e5d5e1122334455667788001200130122334455667788990122334455667788997e7e5d5e112233445566778800120013012233445566778899012233445566778899"));
        bytes.add(Hex.decodeHex("7e7e5d5e1122334455667788001200130122334455667788990122334455667788997e7e5d5e1122334455667788001200130122334455667788990122334455667788997e7e5d5e1122334455667788001200130122334455667788990122334455667788997e7e5d5e1122334455667788001200130122334455667788990122334455667788997e7e5d5e112233445566778800120013012233445566778899012233445566778899"));


        for (int j = 0; j < numberOfTries; j++) {
            FullStackEncoder parser = new FullStackEncoder(34);
            long time = System.currentTimeMillis();
            for (int i = 0; i < testSize; i++) {
                bytes.stream().forEach(parser::read);
            }

            time = System.currentTimeMillis() - time;
            System.out.println(time + " " + callCounter + " " + writerCounter + " " + foundCounter);

        }


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
            if (length == 0 || capacity == size) return 0;

            writerCounter++;
            int writeLen = Math.min(capacity - size, length);
            int newWriterIndex = clip(writerIndex + writeLen);


            if (newWriterIndex > writerIndex || newWriterIndex == 0) {
                System.arraycopy(bytes, offset, buffer, writerIndex, writeLen);
            } else {
                System.arraycopy(bytes, offset, buffer, writerIndex, writeLen - newWriterIndex);
                System.arraycopy(bytes, offset + writeLen - newWriterIndex, buffer, 0, newWriterIndex);
            }


            writerIndex = newWriterIndex;
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


        private final int maxPacketSize;

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
            this.maxPacketSize = maxPacketSize;
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
            callCounter++;
            int remainingBytes = bytes.length - buffer.write(bytes, 0, bytes.length);
            while (buffer.size >= requiredBytes) {

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
                if (buffer.size() < requiredBytes)
                    remainingBytes -= buffer.write(bytes, bytes.length - remainingBytes, remainingBytes);
                if (log)
                    System.out.println(state);
            }


        }

        private void searchingForPacket() {
            if (buffer.size() >= requiredBytes) {
                byte[] readBytes = buffer.read(requiredBytes);
                if (readBytes.length > 0) {
                    foundCounter++;
                }
                if (log) {
                    System.out.println(foundCounter + ": " + Hex.encodeHexString(readBytes) + " " + callCounter);
                }
                buffer.skip(requiredBytes);
                state = STATE.SEARCHING_FOR_SYN;
                requiredBytes = 16;
            }
        }

        private void searchingForTbh() {
            if (buffer.size() >= requiredBytes) {
                byte[] readBytes = buffer.read(requiredBytes);
                packet = new Packet(readBytes);
                if (packet.isValid() && packet.length + 16 <= maxPacketSize) {
                    state = STATE.SEARCHING_FOR_PACKET;
                    requiredBytes = packet.getLength() + 16;
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
                    } else {
                        buffer.skip(buffer.size());
                    }

                }
            }
        }
    }

}
