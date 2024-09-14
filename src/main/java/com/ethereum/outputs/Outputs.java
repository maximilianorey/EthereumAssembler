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

    public static void printCode(Parser parser, String[] argv) throws IOException, ParserException {
        System.out.println(parser.generateCode(new HashMap<>()));
        parser.getParametersSet().forEach(entry -> System.out.println(entry.getKey() + ": [" +  entry.getValue().stream().map(Object::toString).collect(Collectors.joining(",")) + "]"));
    }

    public static void solidityInject(Parser parser, String[] argv) throws IOException, ParserException {
        String code = parser.generateCode(new HashMap<>());
        System.out.println("bytes memory dat = new bytes("+ parser.getTotalLength() +");");
        System.out.println("assembly{");
        int lastIndex = code.length()-64;
        for(int i = 0; i<lastIndex;i+=64){
            System.out.println("\tmstore(add(dat,"+ i/2 + "), 0x" + String.join("",code.substring(i,i+64)) + ")");
        }
        String substring = StringUtils.rightPad(code.substring(lastIndex),64,"0");
        System.out.println("\tmstore(add(dat,"+ lastIndex/2 + "), 0x" + String.join("",substring) + ")");

        parser.getParametersSet().forEach(entry -> {
            String parameterKey = entry.getKey();
            entry.getValue().forEach(position -> System.out.println("\tmstore(add(dat," + position + ")," + parameterKey + ")"));
        });

        System.out.println("\tmstore(proxyAddr,create(0,dat," + parser.getTotalLength() + "))");
        System.out.println("}");
    }
}
