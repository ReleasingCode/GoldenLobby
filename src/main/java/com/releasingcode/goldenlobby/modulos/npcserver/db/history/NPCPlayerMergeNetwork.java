package com.releasingcode.goldenlobby.modulos.npcserver.db.history;

import java.io.Serializable;

public class NPCPlayerMergeNetwork implements Serializable {

    public String uid;
    public String name;
    public String previus_target;
    public String current_target;
    public String history;
    public String found_staff;
}
