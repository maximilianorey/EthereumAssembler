package com.ethereum.parser;

import com.ethereum.exceptions.ParserException;
import com.ethereum.structs.Destination;

import com.ethereum.utils.Codes;

import java.util.*;
import java.util.function.BiConsumer;

public class DestinationsManager {
    private final Map<String, Destination> destination = new HashMap<>();

    public void addDestination(String label, Destination destination, int line) throws ParserException {
        if(Codes.reservedLabels.contains(label)){
            throw new ParserException(line, "LABEL '" + label + "' RESERVED.");
        }
        if(label.contains("+")){
            throw new ParserException(line, "Label can not contains '+'.");
        }
        Destination valueBefore = this.destination.get(label);
        if(valueBefore!=null){
            throw new ParserException(line, "Destination already defined on " + valueBefore.getIndex() + " new value: " + destination.getIndex());
        }
        this.destination.put(label,destination);
    }


    public Destination getDestination(String label){
        return this.destination.get(label);
    }

    public void forEach(BiConsumer<String,Destination> func){
        this.destination.forEach(func);
    }

}
