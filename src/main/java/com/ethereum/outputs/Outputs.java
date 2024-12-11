package com.ethereum.outputs;

import com.ethereum.exceptions.ParserException;
import com.ethereum.parser.Parser;
import com.ethereum.utils.Codes;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Outputs {
    public static void fromTypescriptTemplate(Parser parser, String[] argv) throws IOException, ParserException {
        if(argv.length < 4){
            System.err.println("fromTemplate <assembly> <input_file> <output_file>");
            System.exit(1);
        }
        BufferedReader input = new BufferedReader(new FileReader(argv[2]));
        BufferedWriter writer = new BufferedWriter(new FileWriter(argv[3]));
        String codeStr = parser.generateCode(new HashMap<>());
        for(String line = input.readLine(); line!=null;line = input.readLine()){
            String[] splited = line.split(Codes.parameterLabel);
            if(splited.length != 1){
                for(int i = 1;i<splited.length; i+=2){
                    splited[i] = "[" +  parser.getParameter(splited[i]).map(x -> Integer.toString(x+1)).collect(Collectors.joining(",")) + "]";
                }
                line = String.join("", splited);
            }
            writer.write(line.replace("<BINARYCODE>","0x" + codeStr));
            writer.write("\n");
        }
        input.close();
        writer.close();
    }

    public static void printCode(Parser parser) throws IOException, ParserException {
        System.out.println(parser.generateCode(new HashMap<>()));
        parser.getParametersSet().forEach(entry -> System.out.println(entry.getKey() + ": [" +  entry.getValue().stream().map(Object::toString).collect(Collectors.joining(",")) + "]"));
    }

    public static void solidityInject(Parser parser) throws IOException, ParserException {
        String code = parser.generateCode(new HashMap<>());
        int lengthMod32 = parser.getTotalLength() % 32;
        int fixedLength = lengthMod32==0 ? parser.getTotalLength() : (parser.getTotalLength() + 32 - lengthMod32);
        System.out.println("bytes memory dat = new bytes("+  fixedLength +");");
        System.out.println("assembly{");
        int index;
        for(index = 0; index+64<code.length();index+=64){
            System.out.println("\tmstore(add(dat,"+ index/2 + "), 0x" + String.join("",code.substring(index,index+64)) + ")");
        }
        System.out.println("\tmstore(add(dat,"+ index/2 + "), 0x" + String.join("",StringUtils.rightPad(code.substring(index),64,"0")) + ")");

        parser.getParametersSet().forEach(entry -> {
            String parameterKey = entry.getKey();
            entry.getValue().forEach(position -> System.out.println("\tmstore(add(dat," + position + ")," + parameterKey + ")"));
        });

        System.out.println("\tmstore(proxyAddr,create(0,dat," + parser.getTotalLength() + "))");
        System.out.println("}");
    }
}
