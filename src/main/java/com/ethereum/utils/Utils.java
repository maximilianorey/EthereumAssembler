package com.ethereum.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

public class Utils {
    public static double LOG2 = Math.log(2) * 8;
    public static boolean equalsWithNullCase(Object elem1, Object elem2){
        if(elem1==null) return elem2==null;
        return elem1.equals(elem2);
    }

    public static String extendsHexString(String str, int size){
        if(str.length()==size*2){
            return str;
        }
        if(str.length()<size*2){
            return StringUtils.leftPad(str,size*2,"0");
        }
        return null;
    }

    public static String numberToHexString(int index, int size){
        String indexStr = Integer.toHexString(index);
        if(indexStr.length()==size*2){
            return indexStr;
        }
        if(indexStr.length()<size*2){
            return StringUtils.leftPad(indexStr,size*2,"0");
        }
        return null;
    }

    public static String numberToHexString(int index){
        String res = Integer.toHexString(index);
        if(res.length() % 2 == 0){
            return res;
        }
        return "0" + res;
    }

    public static int calculateSize(int index){
        return (int)Math.ceil(Math.log(index)/ LOG2);
    }

    public static String byteArrayToHex(byte[] arr){
        StringBuilder res = new StringBuilder();
        for (byte b : arr) {
            String str = Integer.toHexString(b);
            res.append(StringUtils.leftPad(str, 2, '0'));
        }
        return res.toString();
    }

    public static <T extends Throwable> void ifLabel(String line, String delimiter, Consumer<String,T> func) throws T{
        String[] splited = line.split(delimiter);
        if(splited.length>1){
            func.accept(splited[1]);
        }
    }

    public static <T,E extends Throwable> void forEach(Collection<T> c, Consumer<T,E> func) throws E {
        for(T elem: c){
            func.accept(elem);
        }
    }

    public static <T,E extends Throwable> void forEach(Collection<T> c, BiConsumer<T,Integer,E> func) throws E {
        int index = 0;
        for(T elem: c){
            func.accept(elem,index);
            index += 1;
        }
    }

    public static <U,T,E extends Throwable> U reduceCollection(Collection<T> input, U base, BiFunction<T,U,U,E> func) throws E {
        U actual = base;
        for(T elem: input){
            actual =func.apply(elem,actual);
        }
        return actual;
    }
}
