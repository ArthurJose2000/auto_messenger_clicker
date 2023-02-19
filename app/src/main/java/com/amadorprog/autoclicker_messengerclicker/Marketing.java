package com.amadorprog.autoclicker_messengerclicker;

public class Marketing {

    public int BEHAVIOR_REQUEST = 1;
    public int BEHAVIOR_CLICK = 2;
    public int BEHAVIOR_SHARE = 3;
    public String id;
    public String affiliate_link;

    public void setMarketing(String id, String affiliate_link) {
        this.id = id;
        this.affiliate_link = affiliate_link;
    }
}
