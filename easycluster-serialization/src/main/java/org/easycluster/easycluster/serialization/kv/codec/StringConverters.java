package org.easycluster.easycluster.serialization.kv.codec;


public class StringConverters {
    private StringConverters() {}
    
    public static final StringConverter CONVERT_TO_INTEGER = new StringConverter() {

        public Object transform(String from) {
            return Integer.parseInt(from);
        }
        
    };
    
    public static final StringConverter CONVERT_TO_BYTE = new StringConverter() {

        public Object transform(String from) {
            return Byte.parseByte(from);
        }
        
    };
    
    public static final StringConverter CONVERT_TO_SHORT = new StringConverter() {

        public Object transform(String from) {
            return Short.parseShort(from);
        }
        
    };
    
    public static final StringConverter CONVERT_TO_LONG = new StringConverter() {

        public Object transform(String from) {
            return Long.parseLong(from);
        }
        
    };
    
    public static final StringConverter CONVERT_TO_FLOAT = new StringConverter() {

        public Object transform(String from) {
            return Float.parseFloat(from);
        }
        
    };
    
    public static final StringConverter CONVERT_TO_DOUBLE = new StringConverter() {

        public Object transform(String from) {
            return Double.parseDouble(from);
        }
        
    };
    
    public static final StringConverter CONVERT_TO_BOOLEAN = new StringConverter() {

      public Object transform(String from) {
          return Boolean.parseBoolean(from);
      }
      
  };
    
    public static final StringConverter CONVERT_TO_STRING = new StringConverter() {

        public Object transform(String from) {
            return from;
        }
        
    };
    
    public static StringConverterFactory getCommonFactory() {
      DefaultStringConverterFactory factory = new DefaultStringConverterFactory();
        
      factory.setConverter(Integer.class, CONVERT_TO_INTEGER)
                .setConverter(Byte.class, CONVERT_TO_BYTE)
                .setConverter(Short.class, CONVERT_TO_SHORT)
                .setConverter(Long.class, CONVERT_TO_LONG)
                .setConverter(Float.class, CONVERT_TO_FLOAT)
                .setConverter(Double.class, CONVERT_TO_DOUBLE)
                .setConverter(Boolean.class, CONVERT_TO_BOOLEAN)
                .setConverter(int.class, CONVERT_TO_INTEGER)
                .setConverter(byte.class, CONVERT_TO_BYTE)
                .setConverter(short.class, CONVERT_TO_SHORT)
                .setConverter(long.class, CONVERT_TO_LONG)
                .setConverter(float.class, CONVERT_TO_FLOAT)
                .setConverter(double.class, CONVERT_TO_DOUBLE)
                .setConverter(boolean.class, CONVERT_TO_BOOLEAN)
                .setConverter(String.class, CONVERT_TO_STRING);
        
      return  factory;
    }
}
