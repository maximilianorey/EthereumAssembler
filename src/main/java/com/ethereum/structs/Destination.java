package com.ethereum.structs;

public class Destination {
    private final int index;
    private final DestinationType destinationType;
    public Destination(int index, DestinationType destinationType){
        this.index = index;
        this.destinationType = destinationType;
    }

    public int getIndex() {
        return index;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Destination anotherDestination){
            return this.index == anotherDestination.index && this.destinationType.equals(anotherDestination.destinationType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return index + destinationType.hashCode();
    }
}
