package com.proxtextchat.network;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NetworkGraph {
    private SimpleGraph<NetworkNode, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);


    public NetworkGraph(HashSet<NetworkNode> nodes) throws ChannelMismatch {

        String channel = null;

        // loop though all input nodes and add them to the graph
        for (NetworkNode node : nodes){

            // make sure all nodes have the same channel.
            if(channel != null){
                if(node.getChannel().equals(channel)){
                    throw new ChannelMismatch("Node at" + node.getLocation() + "doesn't match the expected channel");
                }
                graph.addVertex();


            }

            // if our channel var is null we fill it
            else{
                channel = node.getChannel();
            }
        }

        // determine which nodes should be connected to each other
        for (NetworkNode node1 : nodes) {
            Set<WorldChunk> chunks = node1.getChunks();

            for (NetworkNode node2 : nodes) {
                // Avoid connecting the node to itself, and avoid creating duplicates
                if (node1 != node2 && graph.containsEdge(node1, node2)) {


                    // Check if the second node is within one of node1's chucks forge a connection.
                    for (WorldChunk chunk : chunks) {
                        if (node2.getLocation().equals(chunk)) {
                            // If node2 is in a chunk controlled by node1, add an edge
                            graph.addEdge(node1, node2);

                            // No need to check further chunks if an edge is added.
                            break;
                        }
                    }
                }
            }
        }
    }

    public List<HashSet<NetworkNode>> ExtractSubnets(){
        List<HashSet<NetworkNode>> output = new ArrayList<HashSet<NetworkNode>>();

        while (graph.vertexSet().isEmpty()){

            output.add(ExtractSubnet(GetAnyNode(), new HashSet<NetworkNode>()));
        }


        return output;
    }

    /**
     * Extracts a subnet of connected NetworkNodes starting from the specified node.
     *
     * This method performs a depth-first search to find all nodes that are
     * directly or indirectly connected to the given node. It adds these nodes
     * to the provided output HashSet. If the output HashSet is null, a new
     * HashSet will be created. After processing, the original node is removed
     * from the graph.
     *
     * @param node The starting NetworkNode from which to extract the subnet.
     *             This node will also be included in the output.
     * @param output an empty HashSet&lt;NetworkNode&gt; that accumulates the extracted NetworkNodes.
     *             If this parameter is null, a new HashSet will be created.
     * @return A HashSet containing all NetworkNodes in the extracted subnet.
     */
    private HashSet<NetworkNode> ExtractSubnet(NetworkNode node, HashSet<NetworkNode> output){

        if(output == null){
            output = new HashSet<NetworkNode>();
        }

        output.add(node);
        // get the node
        Set<DefaultEdge> edges = graph.edgesOf(node);

        for (DefaultEdge edge : edges){


            NetworkNode neighbor;

            if(graph.getEdgeSource(edge).equals(node)){
                neighbor = graph.getEdgeTarget(edge);
            }
            else{
                neighbor = graph.getEdgeSource(edge);
            }


            if(neighbor != null && !output.contains(neighbor)){
                output.addAll(ExtractSubnet(neighbor, output));
            }
        }

        graph.removeVertex(node);

        return output;
    }

    private @Nullable NetworkNode GetAnyNode(){
        Set<NetworkNode> vertices = graph.vertexSet();

        if (!vertices.isEmpty()) {
            // Retrieve any node, for example, the first one from the set
            return vertices.iterator().next(); // Get an arbitrary node
        } else {
            return null; // or handle the case where the graph is empty
        }
    }

}

