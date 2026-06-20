package com.focusnode.model;

public class LanMemberDto {
    public String id;
    public String name;
    public String ipAddress;
    public String status;
    public long timeLeftSeconds;
    public boolean isHost;
    public boolean isAudioMuted;

    public LanMemberDto() {}
    
    public LanMemberDto(LanMember member) {
        this.id = member.getId();
        this.name = member.getName();
        this.ipAddress = member.getIpAddress();
        this.status = member.getStatus();
        this.timeLeftSeconds = member.getTimeLeftSeconds();
        this.isHost = member.isHost();
        this.isAudioMuted = member.isAudioMuted();
    }

    public LanMember toLanMember() {
        LanMember m = new LanMember(id, name, ipAddress, isHost);
        m.setStatus(status);
        m.setTimeLeftSeconds(timeLeftSeconds);
        m.setAudioMuted(isAudioMuted);
        return m;
    }
}
