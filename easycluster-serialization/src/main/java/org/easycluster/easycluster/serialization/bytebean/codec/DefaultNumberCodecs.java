/**
 * 
 */
package org.easycluster.easycluster.serialization.bytebean.codec;


/**
 * TODO
 * 
 * @author Archibald.Wang
 * @version $Id: DefaultNumberCodecs.java 14 2012-01-10 11:54:14Z archie $
 */
public class DefaultNumberCodecs {

	private static int b2ui(byte b) {
		//byte: -128~127
		//byte+256: 128~343
		//(byte+256)%256: 0~128
		return (int) (b + 256) % 256;
	}

	private static long b2ul(byte b) {
		return (long) (b + 256) % 256;
	}

	private static NumberCodec littleEndianCodec = new NumberCodec() {

		public int bytes2Int(byte[] bytes, int byteLength) {
			int value = 0;
			for (int i = 0; i < byteLength; i++) {
				value |= b2ui(bytes[i]) << (i * 8);
			}
			return value;
		}

		public long bytes2Long(byte[] bytes, int byteLength) {
			long value = 0;
			for (int i = 0; i < byteLength; i++) {
				value |= b2ul(bytes[i]) << (i * 8);
			}

			return value;
		}

		public short bytes2Short(byte[] bytes, int byteLength) {
			short value = 0;
			for (int i = 0; i < byteLength; i++) {
				value |= b2ui(bytes[i]) << (i * 8);
			}

			return value;
		}
		
		@Override
		public float bytes2Float(byte[] bytes, int byteLength) {
			int value = 0;
			for (int i = 0; i < byteLength; i++) {
				value |= b2ui(bytes[i]) << (i * 8);
			}
			return Float.intBitsToFloat(value);
		}

		@Override
		public double bytes2Double(byte[] bytes, int byteLength) {
			long value = 0;
			for (int i = 0; i < byteLength; i++) {
				value |= b2ul(bytes[i]) << (i * 8);
			}
			return Double.longBitsToDouble(value);
		}

		public byte[] int2Bytes(int value, int byteLength) {
			byte[] bytes = new byte[byteLength];

			for (int i = 0; i < byteLength; i++) {
				int shiftCount = i * 8;
				bytes[i] = (byte) ((value & (0x000000ff << shiftCount)) >> shiftCount);
			}
			return bytes;
		}

		public byte[] long2Bytes(long value, int byteLength) {
			byte[] bytes = new byte[byteLength];

			for (int i = 0; i < byteLength; i++) {
				int shiftCount = i * 8;
				bytes[i] = (byte) ((value & (0x00000000000000ffL << shiftCount)) >> shiftCount);
			}
			return bytes;
		}

		public byte[] short2Bytes(short value, int byteLength) {
			byte[] bytes = new byte[byteLength];

			for (int i = 0; i < byteLength; i++) {
				int shiftCount = i * 8;
				bytes[i] = (byte) ((value & (0x00ff << shiftCount)) >> shiftCount);
			}
			return bytes;
		}
		
		@Override
		public byte[] float2Bytes(float value, int byteLength) {
			byte[] bytes = new byte[byteLength];

			//parse the bits that represent the floating-point number 
			//floatToRawIntBits gives the raw float bits without normalization
			//using floatToRawIntBits is over 5 times as fast as floatToIntBits.
			int x = Float.floatToRawIntBits(value);
			for (int i = 0; i < byteLength; i++) {
				int shiftCount = i * 8;
				bytes[i] = (byte) ((x & (0x000000ff << shiftCount)) >> shiftCount);
			}
			return bytes;
		}

		@Override
		public byte[] double2Bytes(double value, int byteLength) {
			byte[] bytes = new byte[byteLength];

			//parse the the bits that represent the floating-point number
			long x = Double.doubleToRawLongBits(value);
			for (int i = 0; i < byteLength; i++) {
				int shiftCount = i * 8;
				bytes[i] = (byte) ((x & (0x00000000000000ffL << shiftCount)) >> shiftCount);
			}
			return bytes;
		}

		public String convertCharset(String charset) {
			if (charset.equals("UTF-16")) {
				return "UTF-16LE";
			} else {
				return charset;
			}
		}

	};

	private static NumberCodec bigEndianCodec = new NumberCodec() {

		public int bytes2Int(byte[] bytes, int byteLength) {
			int value = 0;
			for (int i = 0; i < byteLength; i++) {
				value |= b2ui(bytes[i]) << ((byteLength - 1 - i) * 8);
			}
			return value;
		}

		public long bytes2Long(byte[] bytes, int byteLength) {
			long value = 0;
			for (int i = 0; i < byteLength; i++) {
				value |= b2ul(bytes[i]) << ((byteLength - 1 - i) * 8);
			}

			return value;
		}

		public short bytes2Short(byte[] bytes, int byteLength) {
			short value = 0;
			for (int i = 0; i < byteLength; i++) {
				value |= b2ui(bytes[i]) << ((byteLength - 1 - i) * 8);
			}

			return value;
		}

		@Override
		public float bytes2Float(byte[] bytes, int byteLength) {
			int value = 0;
			for (int i = 0; i < byteLength; i++) {
				value |= b2ui(bytes[i]) << ((byteLength - 1 - i) * 8);
			}
			return Float.intBitsToFloat(value);
		}

		@Override
		public double bytes2Double(byte[] bytes, int byteLength) {
			long value = 0;
			for (int i = 0; i < byteLength; i++) {
				value |= b2ul(bytes[i]) << ((byteLength - 1 - i) * 8);
			}
			return Double.longBitsToDouble(value);
		}

		public byte[] int2Bytes(int value, int byteLength) {
			byte[] bytes = new byte[byteLength];

			for (int i = 0; i < byteLength; i++) {
				int shiftCount = ((byteLength - 1 - i) * 8);
				bytes[i] = (byte) ((value & (0x000000ff << shiftCount)) >> shiftCount);
			}
			return bytes;
		}

		public byte[] long2Bytes(long value, int byteLength) {
			byte[] bytes = new byte[byteLength];

			for (int i = 0; i < byteLength; i++) {
				int shiftCount = ((byteLength - 1 - i) * 8);
				bytes[i] = (byte) ((value & (0x00000000000000ffL << shiftCount)) >> shiftCount);
			}
			return bytes;
		}

		public byte[] short2Bytes(short value, int byteLength) {
			byte[] bytes = new byte[byteLength];

			for (int i = 0; i < byteLength; i++) {
				int shiftCount = ((byteLength - 1 - i) * 8);
				bytes[i] = (byte) ((value & (0x00ff << shiftCount)) >> shiftCount);
			}
			return bytes;
		}

		@Override
		public byte[] float2Bytes(float value, int byteLength) {
			byte[] bytes = new byte[byteLength];

			//parse the bits that represent the floating-point number 
			//floatToRawIntBits gives the raw float bits without normalization
			//using floatToRawIntBits is over 5 times as fast as floatToIntBits.
			int x = Float.floatToRawIntBits(value);
			for (int i = 0; i < byteLength; i++) {
				int shiftCount = ((byteLength - 1 - i) * 8);
				bytes[i] = (byte) ((x & (0x000000ffL << shiftCount)) >> shiftCount);
			}
			return bytes;
		}

		@Override
		public byte[] double2Bytes(double value, int byteLength) {
			byte[] bytes = new byte[byteLength];

			//parse the the bits that represent the floating-point number
			long x = Double.doubleToRawLongBits(value);
			for (int i = 0; i < byteLength; i++) {
				int shiftCount = ((byteLength - 1 - i) * 8);
				bytes[i] = (byte) ((x & (0x00000000000000ffL << shiftCount)) >> shiftCount);
			}
			return bytes;
		}

		public String convertCharset(String charset) {
			if (charset.equals("UTF-16")) {
				return "UTF-16BE";
			} else {
				return charset;
			}
		}

	};

	public static NumberCodec getBigEndianNumberCodec() {
		return bigEndianCodec;
	}

	public static NumberCodec getLittleEndianNumberCodec() {
		return littleEndianCodec;
	}
}
