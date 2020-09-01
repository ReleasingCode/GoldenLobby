package com.releasingcode.goldenlobby.modulos.npcserver.db.history;

import java.io.Serializable;

public class NPCHistoryPlayerNetwork implements Serializable {
    private static final long serialVersionUID = 4L;
    public String uid;
    public String name;
    public String previus_target;
    public String current_target;
    public String history;
    public boolean history_is_playing;
    public String history_current_playing;
    public int history_dash;
    public String staff_found;
}
