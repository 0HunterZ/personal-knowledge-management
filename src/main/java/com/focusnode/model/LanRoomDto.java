package com.focusnode.model;

import java.util.ArrayList;
import java.util.List;

public class LanRoomDto {
    public String id;
    public String name;
    public String hostIp;
    public int port;
    public int memberCount;
    public int maxMembers;
    public List<LanMemberDto> members = new ArrayList<>();

    public LanRoomDto() {}

    public LanRoomDto(LanRoom room) {
        this.id = room.getId();
        this.name = room.getName();
        this.hostIp = room.getHostIp();
        this.port = room.getPort();
        this.memberCount = room.getMemberCount();
        this.maxMembers = room.getMaxMembers();
        for (LanMember m : room.getMembers()) {
            this.members.add(new LanMemberDto(m));
        }
    }

    public LanRoom toLanRoom() {
        LanRoom r = new LanRoom(id, name, hostIp, port);
        r.setMemberCount(memberCount);
        r.setMaxMembers(maxMembers);
        for (LanMemberDto dto : members) {
            r.getMembers().add(dto.toLanMember());
        }
        return r;
    }
}
