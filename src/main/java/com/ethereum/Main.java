package com.ethereum;

import com.ethereum.exceptions.ParserException;
import com.ethereum.outputs.Outputs;
import com.ethereum.parser.Parser;

import java.io.*;

public class Main {
    private static void printError(){
        System.err.println("fromTemplate <assembly> <input_file> <output_file>");
        System.err.println("printCode <assembly>");
        System.err.println("solidityInject <assembly>");
    }
    public static void main(String[] argv) throws IOException, ParserException {
        if(argv.length < 2){
            printError();
            System.exit(1);
        }
        boolean hardhat2Compatibility = argv[argv.length-1].equals("--hardhat-2-compatibility");
        if(hardhat2Compatibility){
            System.err.println("USING HARDHAT 2 COMPATIBILITY");
        }else{
            System.err.println("HARDHAT 2 COMPATIBILITY DISABLED");
        }
        Parser parser = new Parser(new File(argv[1]),hardhat2Compatibility);
        parser.processFile();
        switch (argv[0]){
            case "fromTemplate":
                Outputs.fromTypescriptTemplate(parser,argv);
                break;
            case "printCode":
                Outputs.printCode(parser,argv);
                break;
            case "solidityInject":
                Outputs.solidityInject(parser,argv);
                break;
            default:
                printError();
        }
    }
}
