package com.ethereum.parser;

import com.ethereum.exceptions.ParserException;
import com.ethereum.structs.*;
import com.ethereum.utils.Codes;
import com.ethereum.utils.Utils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class Parser {
    private final File file;
    private final DestinationsManager constructorDestinationsManager = new DestinationsManager();
    private final DestinationsManager bodyDestinationsManager = new DestinationsManager();
    private final Map<String,List<Integer>> parameters = new HashMap<>();
    private final Map<String,List<String>> errors = new HashMap<>();

    private int constructorSize = 0;
    private int bodySize = 0;
    private final boolean hardhat2Compatibility;

    private String directionForLabel(DestinationsManager set, String label, int line, int pushSize, String instruction) throws ParserException {
        Destination destination = switch (label) {
            case "CONSTRUCTOR LENGTH" -> new Destination(constructorSize,DestinationType.OTHER);
            case "CODE LENGTH" -> new Destination(bodySize,DestinationType.OTHER);
            case "TOTAL LENGTH" -> new Destination(constructorSize + bodySize,DestinationType.OTHER);
            default -> set.getDestination(label);
        };
        if(destination==null){
            throw new ParserException(line, "Label destination not found: " + label);
        }
        String destinationStr = Utils.numberToHexString(destination.getIndex());
        String res = Utils.extendsHexString(destinationStr,pushSize);
        if(res==null){
            throw new ParserException(line, "For label: '" + label + "' Destination pointer too int (size on origin: " + pushSize + " pointer '0x" + destinationStr + "' size: " + destinationStr.length()/2 + ")");
        }
        if(!destination.getDestinationType().equals(DestinationType.DESTJUMP) && instruction.equals("57")){
            throw new ParserException(line, "Jump to non DESTJUMP instruction (57). Instruction is: '0x" + instruction + "'" );
        }
        return res;
    }

    private String generateError(String errorStr, int index){
        /*
            0x60 - 0x7F (PUSH)
            0x-- // ERROR LENGTH
            ...
            0x--
            0x60 // PUSH1
            0x00 // 0
            0x81 // DUP2(ERROR LENGTH)
            0x60 - 0x7F (PUSH)
            0x-- // ERROR INDEX
            ...
            0x--
            0x82 // DUP3 (0)
            0x39 // CODECOPY
            0xFD // REVERT
         */

        String error = Utils.byteArrayToHex(errorStr.getBytes());
        StringBuilder message = new StringBuilder();
        if(this.hardhat2Compatibility){
            message.append("08c379a00000000000000000000000000000000000000000000000000000000000000020");
            message.append(Utils.numberToHexString(error.length()/2,32));
            message.append(error);
            int noAlignedSize = 36 + 32 + error.length()/2;
            if((noAlignedSize - 4) % 32 != 0){
                message.append(StringUtils.rightPad("",32 - ((noAlignedSize - 4) % 32),'0'));
            }
        } else{
            message.append("08c379a0");
            String errorLength = Utils.numberToHexString(error.length()/2);
            message.append(Utils.numberToHexString(errorLength.length()/2,32));
            message.append(errorLength);
            message.append(error);
        }

        int errorLength = error.length()/2;
        int messageLength = message.length()/2;
        int newIndexLength = Utils.calculateSize(index + 8 + Utils.calculateSize(errorLength) + 32 + messageLength);

        int newIndex = index + 8 + Utils.calculateSize(errorLength) + newIndexLength + messageLength;

        StringBuilder res = new StringBuilder();
        res.append(Utils.numberToHexString(0x60 + Utils.calculateSize(messageLength) - 1));
        res.append(Utils.numberToHexString(messageLength));
        res.append("600081");

        res.append(Utils.numberToHexString(0x60 + newIndexLength - 1));
        res.append(Utils.numberToHexString(newIndex,newIndexLength));
        res.append("8239FD");
        res.append(message);

        return res.toString();
    }

    private Tuple<Integer,Integer> process(DestinationsManager destinationsManager, Function<String,Boolean> endCondition, int initialLine) throws ParserException, IOException {
        BufferedReader input = new BufferedReader(new FileReader(this.file));
        int index = 0;
        int lineNumber = initialLine;
        for(String line = input.readLine();endCondition.apply(line); line = input.readLine()){
            String _line = line.trim();
            if(!_line.isEmpty() && !_line.startsWith("//")) {
                int _index = index;
                int _lineNumber = lineNumber;
                if(_line.startsWith(Codes.errorLabel)){
                    String error = _line.substring(Codes.errorLabel.length());
                    String errorCode = generateError(error,_index);
                    index += errorCode.length();
                    this.errors.computeIfAbsent(error, k -> new LinkedList<>()).add(errorCode);
                }else if(_line.startsWith("0x")){
                    Utils.ifLabel(_line, Codes.destinationLabel,label -> {
                        String code = _line.substring(2, 4);
                        destinationsManager.addDestination(label, new Destination(_index, code.equals("5B") ? DestinationType.DESTJUMP : DestinationType.OTHER),_lineNumber);
                    });
                    if (_line.contains(Codes.sourceLabel) || _line.contains(Codes.parameterLabel)) {
                        String code = _line.substring(2, 4);
                        Integer size = Codes.pushCodes.get(code);
                        if (size == null) {
                            throw new ParserException(_lineNumber, "code with source label is not a push: " + code);
                        }
                        index += size;
                    }

                    Utils.ifLabel(_line,Codes.parameterLabel, label -> this.parameters.computeIfAbsent(label, k -> new LinkedList<>()).add(_index));
                    index += 1;
                }
            }
            lineNumber += 1;
        }
        input.close();
        return new Tuple<>(index,lineNumber + 1);
    }

    public void processFile() throws IOException, ParserException {
        Tuple<Integer,Integer> constructorResult = process(constructorDestinationsManager, line -> !line.equals("END CONSTRUCTOR"),1);
        this.constructorSize = constructorResult.getKey();
        this.bodySize = process(bodyDestinationsManager,  Objects::nonNull,constructorResult.getValue()).getKey();
    }

    private Tuple<Integer,List<String>> generateCodeAux(BufferedReader input, DestinationsManager set, Function<String,Boolean> endCondition, int initialLineNumber, Map<String,Integer> parameters) throws ParserException, IOException {
        List<String> res = new LinkedList<>();
        int lineNumber = initialLineNumber;
        for(String line = input.readLine(); endCondition.apply(line); line = input.readLine()){
            String _line = line.trim();
            if(!_line.isEmpty() && !_line.startsWith("//")) {
                if(_line.startsWith(Codes.errorLabel)){
                    res.add(this.errors.get(_line.substring(Codes.errorLabel.length())).remove(0));
                }else if(_line.startsWith("0x")){
                    String instruction = _line.substring(2, 4);
                    res.add(instruction);
                    int _lineNumber = lineNumber;
                    Utils.ifLabel(_line,Codes.sourceLabel,label -> {
                        Integer pushSize = Codes.pushCodes.get(instruction);
                        if(pushSize==null){
                            throw new ParserException(_lineNumber,"Instruction is not a push. Instruction found: '0x" + instruction + "'");
                        }
                        res.add(directionForLabel(set,label,_lineNumber, pushSize, instruction));
                    });
                    Utils.ifLabel(_line,Codes.parameterLabel,label -> {
                        Integer pushSize = Codes.pushCodes.get(instruction);
                        if(pushSize==null){
                            throw new ParserException(_lineNumber,"Instruction is not a push. Instruction found: '0x" + instruction + "'");
                        }
                        Integer parameterForLabel = parameters.get(label);
                        if(parameterForLabel==null){
                            res.add(StringUtils.leftPad("",pushSize*2,'0'));
                        }else{
                            String parameterForLabelStr = Utils.numberToHexString(parameterForLabel);
                            if(parameterForLabelStr.length() < pushSize*2){
                                throw new ParserException(_lineNumber,"For label: '" + label + "' Parameter value too int (size on origin: " + pushSize + " pointer size: " + pushSize + ")");
                            }
                            res.add(StringUtils.leftPad(parameterForLabelStr,pushSize*2,'0'));
                        }
                    });
                }
            }
            lineNumber += 1;
        }
        return new Tuple<>(lineNumber+1,res);
    }

    public String generateCode(Map<String,Integer> parameters) throws ParserException, IOException {
        BufferedReader input = new BufferedReader(new FileReader(this.file));
        Tuple<Integer,List<String>> constructor = generateCodeAux(input,this.constructorDestinationsManager, line -> !line.equals("END CONSTRUCTOR"),1,parameters);
        List<String> body = generateCodeAux(input,this.bodyDestinationsManager, Objects::nonNull,constructor.getKey(),parameters).getValue();
        input.close();

        List<String> res = new LinkedList<>();
        res.addAll(constructor.getValue());
        res.addAll(body);

        return String.join("", res);
    }

    public Stream<Integer> getParameter(String label){
        return this.parameters.get(label).stream();
    }

    public Set<Map.Entry<String,List<Integer>>> getParametersSet(){
        return this.parameters.entrySet();
    }

    public int getTotalLength(){
        return this.bodySize + this.constructorSize;
    }

    public Parser(File file, boolean hardhat2Compatibility) throws IOException {
        this.file = file;
        this.hardhat2Compatibility = hardhat2Compatibility;
    }
}
