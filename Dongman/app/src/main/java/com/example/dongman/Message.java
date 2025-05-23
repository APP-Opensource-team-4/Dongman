package com.example.dongman;

public class Message {
    public enum Type { LEFT, RIGHT }
    public final Type type;
    public final String sender;
    public final String text;
    public Message(Type t, String s, String m) { type = t; sender = s; text = m; }
}
