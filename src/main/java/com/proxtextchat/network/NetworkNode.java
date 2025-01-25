package com.proxtextchat.network;


import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NetworkNode {

    private static final List<Integer> idCounter = new ArrayList<>();
    private final Integer ID;

    private static Integer generateId(){

        if (idCounter.isEmpty()){
            idCounter.add(1);
            return 1;
        }

        while(true){
            Integer lastNum = idCounter.getLast();

            if(!idCounter.contains(lastNum++)){
                return lastNum;
            }
        }
    }
    private final WorldChunk Location;

    private final Identifier Channel;

    private final Set<WorldChunk> Range;


    public NetworkNode(Identifier channel, WorldChunk location, Set<WorldChunk> chunks){
        Channel = channel;
        Location = location;
        Range = chunks;

        ID = generateId();
    }

    public NetworkNode(Identifier channel, WorldChunk location, Set<WorldChunk> chunks, Integer id){
        if (idCounter.contains(id)){
            throw new IllegalArgumentException("id `" + id + "` has already been assigned");
        }

        ID = id;
        idCounter.add(id);

        Channel = channel;
        Location = location;
        Range = chunks;
    }

    public WorldChunk getLocation() {
        return Location;
    }

    public Identifier getChannel() {
        return Channel;
    }

    public Set<WorldChunk> getRange() {
        return Range;
    }

    public Integer getID(){ return ID;}
}


