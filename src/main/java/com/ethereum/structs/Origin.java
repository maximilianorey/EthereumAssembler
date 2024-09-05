package com.ethereum.structs;

public class Origin {
    private final long position;
    private final long line;
    private final int size;
    private final String label;
    private final int hashcode;
    public Origin(long position, long line, int size, String label){
        this.position = position;
        this.size = size;
        this.label = label;
        this.line = line;
        this.hashcode = (int)(position % Integer.MAX_VALUE) + (int)(line % Integer.MAX_VALUE) + size + label.hashCode();
    }

    public long getPosition() {
        return position;
    }

    public int getSize() {
        return size;
    }

    public String getLabel() {
        return label;
    }

    public long getLine() {
        return line;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Origin anotherOrigin){
            return this.position == anotherOrigin.position && this.line == anotherOrigin.line && this.size == anotherOrigin.size && this.label.equals(anotherOrigin.label);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.hashcode;
    }
}
