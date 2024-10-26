package com.proxtextchat.network;

import net.minecraft.world.chunk.Chunk;

import java.util.HashSet;

public class Network {

    public final String Channel;

    public final HashSet<NetworkNode> Nodes;

    public final HashSet<Chunk> Range;

    public Network(HashSet<NetworkNode> nodes) throws ChannelMismatch {

        String tempChannel = null;

        HashSet<NetworkNode> tempNodes = new HashSet<>();
        HashSet<Chunk> tempRange = new HashSet<>();

        for (NetworkNode node : nodes){
            // check if we have defined channel yet.
            if(tempChannel == null){
                tempChannel = node.getChannel();
            }
            else{
                if (node.getChannel() != tempChannel){
                    throw new ChannelMismatch("All channels in a network must match");
                }
            }

            tempNodes.add(node);

            tempRange.addAll(node.getChunks());


        }
        Channel = tempChannel;
        Nodes = tempNodes;
        Range = tempRange;
    }

}

