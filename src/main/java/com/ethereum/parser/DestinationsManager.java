package com.ethereum.parser;

import com.ethereum.exceptions.ParserException;
import com.ethereum.structs.Destination;

import com.ethereum.utils.Codes;

import java.util.*;

public class DestinationsManager {
    private final Map<String, Destination> destination = new HashMap<>();

    public void addDestination(String label, Destination destination, int line) throws ParserException {
        if(Codes.reservedLabels.contains(label)){
            throw new ParserException(line, "LABEL '" + label + "' RESERVED.");
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

}
