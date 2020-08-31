package com.releasingcode.goldenlobby.managers;

public class MessageSuggest {
    public String text;
    public String suggestcmd;
    public String hoverText;

    public MessageSuggest(String text, String suggestcmd, String hoverText) {
        this.text = text;
        this.suggestcmd = suggestcmd;
        this.hoverText = hoverText;
    }
}
