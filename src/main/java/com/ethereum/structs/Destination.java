package com.ethereum.structs;

public class Destination {
    private final long index;
    private final DestinationType destinationType;
    public Destination(long index, DestinationType destinationType){
        this.index = index;
        this.destinationType = destinationType;
    }

    public long getIndex() {
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
        return (int)(index % Integer.MAX_VALUE) + destinationType.hashCode();
    }
}
