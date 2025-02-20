package com.proxtextchat.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.*;
import java.util.function.IntFunction;

/**
 * A network of nodes and tools for passing messages between them.
 */
public class Channel implements Collection<NetworkNode>{
    private final DirectedMultigraph<NetworkNode, DefaultEdge> Graph = new DirectedMultigraph<>(DefaultEdge.class);

    private final HashMap<ChunkReference, HashSet<NetworkNode>> NodeReceivingRegistry = new HashMap<>();

    private final HashSet<PlayerEntity> ReceiveFromPlayerRegistry = new HashSet<>();

    private final HashSet<PlayerEntity> SendToPlayerRegistry = new HashSet<>();

    private Identifier ID = null;

    private static final String NodeListKey = "NodeList";

    /**
     * provides the necessary logic for translating
     * between in-memory objects and their external representations.
     * See <a href="https://docs.fabricmc.net/1.21/develop/codecs">Fabric Codec Docs</a>.
     */
    public static final Codec<Channel> CODEC;

    static{
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                NetworkNode.CODEC.listOf().fieldOf(NodeListKey).validate(Channel::ValidateNodeList).forGetter(Channel::getNodeList)
        ).apply(instance, inner-> {
            try {
                return new Channel(inner);
            } catch (ChannelMismatch e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private static DataResult<List<NetworkNode>> ValidateNodeList(List<NetworkNode> nodes){
        Identifier channel = null;

        for(NetworkNode node : nodes){
            if(channel != null){
                if(node.getChannelId() != channel){
                    return DataResult.error(()->"Channel mismatch in the node list. All nodes must have the same channel.");
                }
            }
            else{
                channel = node.getChannelId();
            }
        }

        return DataResult.success(nodes);
    }

    private ArrayList<NetworkNode> getNodeList(){
        return new ArrayList<>(Graph.vertexSet());
    }

    /**
     * @param nodes Nodes to make up the channel
     * @throws ChannelMismatch If any node's channelId's don't match.
     */
    public Channel(Collection<NetworkNode> nodes) throws ChannelMismatch {
        this(new HashSet<>(nodes));
    }

    /**
     * Constructs a Channel by adding the provided nodes to the graph and establishing connections
     * between them based on their receiving chunks.
     * Ensures that all nodes have the same channel ID.
     * The nodes are added to the graph and connections are made between them if they share common chunks.
     *
     * @param nodes the set of nodes to be added to the channel.
     * @throws ChannelMismatch if any node does not match the expected channel.
     */
    public Channel(@NotNull HashSet<NetworkNode> nodes) throws ChannelMismatch {

        // a registry of nodes that lets us avoid looping though the
        // node set a second time.

        // loop though all input nodes and add them to the graph
        for (NetworkNode node : nodes){

            // make sure all nodes have the same channel.
            if(ID != null){
                if(!node.getChannelId().equals(ID)){
                    throw new ChannelMismatch("Node at" + node.getReceivingChunks() + "doesn't match the expected channel");
                }
            }

            // if our channel var is null, we fill it
            else{
                ID = node.getChannelId();
            }

            // if we haven't thrown an exception, add the node to the graph.
            Graph.addVertex();
            for(ChunkReference chunk : node.getReceivingChunks()){
                // Remember where the node is
                if(!NodeReceivingRegistry.containsKey(chunk)){
                    NodeReceivingRegistry.put(chunk, new HashSet<>());
                }

                NodeReceivingRegistry.get(chunk).add(node);

            }
        }

        // determine which nodes should be connected to each other
        for (NetworkNode node1 : nodes) {
            Set<ChunkReference> chunks = node1.getRangeChunks();

            Set<ChunkReference> intersection = new HashSet<>(chunks);

            intersection.retainAll(NodeReceivingRegistry.keySet());

            // loop though all the chunks in Node1
            for (ChunkReference chunk : intersection) {

                // if there is, loop though all nodes in that chunk
                for (NetworkNode node2 : NodeReceivingRegistry.get(chunk)){
                    // dont connect to ourself
                    if (node2 != node1) {
                        // If node2 is in a chunk controlled by node1, add an edge
                        Graph.addEdge(node1, node2);
                    }
                }

            }
        }
    }


    /**
     * DOES NOT UPDATE ShortestPathRegistry! Make sure you update after you're done
     *
     * @param node the node to be added.
     * @return Returns true if the collection has been added.
     */
    private boolean AddNodeNoUpdate(@NotNull NetworkNode node){
        // make sure the node is not already in the graph.
        if(Graph.containsVertex(node)){
            return false;
        }

        // make sure the node is in the right channel.
        if(node.getChannelId() != ID){
            return false;
        }

        //first, we add the node to the graph
        Graph.addVertex(node);

        // then we establish outgoing connections from the node.
        for (ChunkReference chunk : node.getRangeChunks()){
            if (NodeReceivingRegistry.containsKey(chunk)){
                for (NetworkNode node2 : NodeReceivingRegistry.get(chunk)){
                    Graph.addEdge(node, node2);
                }
            }
        }

        //now we register the location of the node
        for(ChunkReference chunk : node.getReceivingChunks()){
            if (!NodeReceivingRegistry.containsKey(chunk)){
                NodeReceivingRegistry.put(chunk, new HashSet<>());
            }
            NodeReceivingRegistry.get(chunk).add(node);
        }



        // now we establish incoming connections
        for (NetworkNode node1 : Graph.vertexSet()){
            // if receiving and range have chunks in common...
            if(!Collections.disjoint(node.getReceivingChunks(), node1.getRangeChunks())){
                Graph.addEdge(node1, node);
            }
        }

        return true;
    }

    /**
     * Adds a node to the graph and establishes connections for it based on its range and receiving chunks.
     *
     * @param node the node to be added to the graph.
     * @return {@code true} if the node was added to the channel. {@code false} if the node's channel doesn't match
     */
    @Override
    public boolean add(@NotNull NetworkNode node){

        boolean output = AddNodeNoUpdate(node);

        if(output == true){
            // finally, our ShortedPathRegistry is now out of date and will need to be recalculated.
            ShortestPathRegistry = new HashMap<>();

            PopulateShortestPathRegistry();
        }
        return output;
    }


    /**
     * Merges the nodes from one channel into another.
     * @param channel Another Channel you wish to combine with this channel
     */
    public void MergeChannels(@NotNull Channel channel){
        if(channel.getID() == ID && channel != this){
            this.addAll(channel.getNodeList());
        }
    }
    /**
     * same as addNode, but creates the node instead of receiving a node object.
     * @param receivingChunks the range of chunks where the node can receive messages from
     * @param rangeChunks the range of chunks the node can send messages to.
     */
    public void NewNode(Set<ChunkReference> receivingChunks, Set<ChunkReference> rangeChunks){
        NetworkNode node = new NetworkNode(ID, receivingChunks, rangeChunks);

        add(node);

    }

    /**
     * DOES NOT UPDATE ShortestPathRegistry! Make sure you update after you're done
     * @param node the node you would like to remove
     * @return returns true if the collection has been changed.
     */
    private boolean RemoveNodeNoUpdate(@NotNull NetworkNode node){
        if (!Graph.containsVertex(node)) {
            return false;
        }

        Graph.removeVertex(node);

        return true;
    }

    /**
     * Removes a node from the graph and clears any associated data.
     * @param node the node to be removed from the graph.
     */
    @Override
    public boolean remove(@NotNull Object node) {
        if (!(node instanceof NetworkNode networkNode)) {
            return false;
        }

        boolean output = RemoveNodeNoUpdate(networkNode);

        if (output == true){
            ShortestPathRegistry.clear();
            PopulateShortestPathRegistry();
        }


        return output;
    }

    private HashMap<NetworkNode /*origin*/, HashMap<NetworkNode/*destination*/, ArrayList<NetworkNode>/*path*/>>
            ShortestPathRegistry = new HashMap<>();

    /**
     * Retrieves the shortest path between the origin and destination nodes, if available,
     * from the shortest path registry.
     *
     * @param origin the starting node for the message.
     * @param destination the target node for the message.
     * @return an ArrayList of nodes representing the shortest path from origin to destination,
     *         or null if no path is found.
     */
    public ArrayList<NetworkNode> DirectMessage(@NotNull NetworkNode origin, @NotNull NetworkNode destination){
        PopulateShortestPathRegistry();

        if (!ShortestPathRegistry.containsKey(origin)) {
            return null;
        }

        return ShortestPathRegistry.get(origin).get(destination);
    }

    /**
     * Retrieves all broadcast paths from the origin node to all other reachable nodes,
     * using the shortest path registry.
     *
     * @param origin the starting node for the broadcast message.
     * @return a HashMap mapping each reachable node to its shortest path from the origin node,
     *         or null if no paths are found.
     */
    public HashMap<NetworkNode, ArrayList<NetworkNode>> BroadcastPaths(@NotNull NetworkNode origin){
        PopulateShortestPathRegistry();
        if (!ShortestPathRegistry.containsKey(origin)){
            return null;
        }

        return ShortestPathRegistry.get(origin);
    }


    //TODO: figure threading and implement here.
    private void PopulateShortestPathRegistry(){
        if (ShortestPathRegistry.isEmpty()){



            // each loop on this node is independent, not dependent on the previous,
            // so we shouldn't have issues threading this process
            for (NetworkNode source : Graph.vertexSet()) {

                ShortestPathRegistry.put(source, computeShortestPaths(source));
            }
        }
    }

    private @NotNull HashMap<NetworkNode, ArrayList<NetworkNode>> computeShortestPaths(@NotNull NetworkNode source) {
        if (source.getChannelId() != ID){
            return new HashMap<>();
        }
        else if (Graph.containsVertex(source)){
            return new HashMap<>();
        }

        // Map to store the shortest path from the source to each node
        HashMap<NetworkNode, ArrayList<NetworkNode>> shortestPaths = new HashMap<>();

        // Map to store the minimum distance from the source to each node
        HashMap<NetworkNode, Double> distances = new HashMap<>();

        // Priority queue to process nodes in order of distance
        PriorityQueue<NetworkNode> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        // Initialize distances to infinity and paths to empty
        for (NetworkNode node : Graph.vertexSet()) {
            distances.put(node, Double.POSITIVE_INFINITY);
            shortestPaths.put(node, new ArrayList<>());
        }

        // Set the distance to the source as 0
        distances.put(source, 0.0);
        priorityQueue.add(source);
        shortestPaths.get(source).add(source); // Source's path to itself is just itself

        // Dijkstra's algorithm, re implemented to save some compute time
        while (!priorityQueue.isEmpty()) {
            NetworkNode current = priorityQueue.poll();

            for (DefaultEdge edge : Graph.outgoingEdgesOf(current)) {
                NetworkNode neighbor = Graph.getEdgeTarget(edge);
                if (neighbor.equals(current)) {
                    neighbor = Graph.getEdgeSource(edge);
                }

                double weight = 1.0; // Adjust if your edges have weights
                double newDistance = distances.get(current) + weight;

                if (distances.containsKey(neighbor) && newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);

                    // Update the path to the neighbor
                    ArrayList<NetworkNode> path = new ArrayList<>(shortestPaths.get(current));
                    path.add(neighbor);
                    shortestPaths.put(neighbor, path);

                    priorityQueue.add(neighbor);
                }
            }
        }

        return shortestPaths;
    }

    public @NotNull Map<ChunkReference, HashSet<NetworkNode>> getNodeReceivingRegistry(){
        return Collections.unmodifiableMap(NodeReceivingRegistry);
    }

    public @NotNull Set<PlayerEntity> getReceiveFromPlayerRegistry(){
        return Collections.unmodifiableSet(ReceiveFromPlayerRegistry);
    }

    public @NotNull Set<PlayerEntity> getSendToPlayerRegistry() {
        return Collections.unmodifiableSet(SendToPlayerRegistry);
    }

    /**
     * Adds a player to the registry of players' Channel will receive messages.
     *
     * @param player the player to be added to the receiving registry.
     */
    public void addReceivingFromPlayer(@NotNull PlayerEntity player){
        ReceiveFromPlayerRegistry.add(player);
    }

    /**
     * Adds a player to the registry of players' Channel will send messages to.
     *
     * @param player the player to be added to the send to registry.
     */
    public void addSendToPlayer(@NotNull PlayerEntity player){
        SendToPlayerRegistry.add(player);
    }

    /**
     * Removes a player from the registry of players' Channel will receive messages.
     *
     * @param player the player to be removed from the receiving registry.
     */
    public void removeReceivingFromPlayer(@NotNull PlayerEntity player){
        ReceiveFromPlayerRegistry.remove(player);
    }

    /**
     * Removes a player from the registry of players' Channel will send messages to.
     *
     * @param player the player to be removed from the send to registry.
     */
    public void removeSendToPlayer(@NotNull PlayerEntity player){
        SendToPlayerRegistry.remove(player);
    }

    /**
     * Checks if a player is registered to receive messages from a channel.
     *
     * @param player the player to check.
     * @return true if the player is registered to receive messages, false otherwise.
     */
    public boolean hasReceivingFromPlayer(@NotNull PlayerEntity player){
        return ReceiveFromPlayerRegistry.contains(player);
    }

    /**
     * Checks if a player is registered to send messages to a channel.
     *
     * @param player the player to check.
     * @return true if the player is registered to send messages, false otherwise.
     */
    public boolean hasSendToPlayer(@NotNull PlayerEntity player){
        return SendToPlayerRegistry.contains(player);
    }


    /**
     * Returns the number of elements in this collection.  If this collection
     * contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of elements in this collection
     */
    @Override
    public int size() {
        return Graph.vertexSet().size();
    }

    public boolean isEmpty(){
        return Graph.vertexSet().isEmpty();
    }

    /**
     * Returns {@code true} if this collection contains the specified element.
     * More formally, returns {@code true} if and only if this collection
     * contains at least one element {@code e} such that
     * {@code Objects.equals(o, e)}.
     *
     * @param o element whose presence in this collection is to be tested
     * @return {@code true} if this collection contains the specified
     * element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this collection
     *                              ({@linkplain Collection##optional-restrictions optional})
     * @throws NullPointerException if the specified element is null and this
     *                              collection does not permit null elements
     *                              ({@linkplain Collection##optional-restrictions optional})
     */
    @Override
    public boolean contains(Object o) {
        return Graph.vertexSet().contains(o);
    }

    /**
     * Returns an iterator over the elements in this collection.  There are no
     * guarantees concerning the order in which the elements are returned
     * (unless this collection is an instance of some class that provides a
     * guarantee).
     *
     * @return an {@code Iterator} over the elements in this collection
     */
    @NotNull
    @Override
    public Iterator<NetworkNode> iterator() {
        return Graph.vertexSet().iterator();
    }

    /**
     * Returns an array containing all the elements in this collection.
     * If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order. The returned array's {@linkplain Class#getComponentType
     * runtime component type} is {@code Object}.
     *
     * <p>The returned array will be "safe" in that this collection
     * maintains no references to it.  (In other words, this method must
     * allocate a new array even if this collection is backed by an array).
     * The caller is thus free to modify the returned array.
     *
     * @return an array, whose {@linkplain Class#getComponentType runtime component
     * type} is {@code Object}, containing all the elements in this collection
     * @apiNote This method acts as a bridge between array-based and collection-based APIs.
     * It returns an array whose runtime type is {@code Object[]}.
     * Use {@link #toArray(Object[]) toArray(T[])} to reuse an existing
     * array, or use {@link #toArray(IntFunction)} to control the runtime type
     * of the array.
     */
    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return Graph.vertexSet().toArray();
    }

    /**
     * Returns an array containing all the elements in this collection;
     * the runtime type of the returned array is that of the specified array.
     * If the collection fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this collection.
     *
     * <p>If this collection fits in the specified array with room to spare
     * (i.e., the array has more elements than this collection), the element
     * in the array immediately following the end of the collection is set to
     * {@code null}.  (This is useful in determining the length of this
     * collection <i>only</i> if the caller knows that this collection does
     * not contain any {@code null} elements.)
     *
     * <p>If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order.
     *
     * @param a the array into which the elements of this collection are to be
     *          stored, if it is big enough; otherwise, a new array of the same
     *          runtime type is allocated for this purpose.
     * @return an array containing all the elements in this collection
     * @throws ArrayStoreException  if the runtime type of any element in this
     *                              collection is not assignable to the {@linkplain Class#getComponentType
     *                              runtime component type} of the specified array
     * @throws NullPointerException if the specified array is null
     * @apiNote This method acts as a bridge between array-based and collection-based APIs.
     * It allows an existing array to be reused under certain circumstances.
     * Use {@link #toArray()} to create an array whose runtime type is {@code Object[]},
     * or use {@link #toArray(IntFunction)} to control the runtime type of
     * the array.
     *
     * <p>Suppose {@code x} is a collection known to contain only strings.
     * The following code can be used to dump the collection into a previously
     * allocated {@code String} array:
     *
     * <pre>
     *     String[] y = new String[SIZE];
     *     ...
     *     y = x.toArray(y);</pre>
     *
     * <p>The return value is reassigned to the variable {@code y}, because a
     * new array will be allocated and returned if the collection {@code x} has
     * too many elements to fit into the existing array {@code y}.
     *
     * <p>Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     */
    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        return Graph.vertexSet().toArray(a);
    }



    /**
     * Returns {@code true} if this collection contains all the elements
     * in the specified collection.
     *
     * @param c collection to be checked for containment in this collection
     * @return {@code true} if this collection contains all the elements
     * in the specified collection
     * @throws ClassCastException   if the types of one or more elements
     *                              in the specified collection are incompatible with this
     *                              collection
     *                              ({@linkplain Collection##optional-restrictions optional})
     * @throws NullPointerException if the specified collection contains one
     *                              or more null elements and this collection does not permit null
     *                              elements
     *                              ({@linkplain Collection##optional-restrictions optional})
     *                              or if the specified collection is null.
     * @see #contains(Object)
     */
    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return Graph.vertexSet().containsAll(c);
    }

    /**
     * Adds all the elements in the specified collection to this collection
     * (optional operation).  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in progress.
     * (This implies that the behavior of this call is undefined if the
     * specified collection is this collection, and this collection is
     * nonempty.) If the specified collection has a defined
     * <a href="SequencedCollection.html#encounter">encounter order</a>,
     * processing of its elements generally occurs in that order.
     *
     * @param c collection containing elements to be added to this collection
     * @return {@code true} if this collection changed as a result of the call
     * @throws UnsupportedOperationException if the {@code addAll} operation
     *                                       is not supported by this collection
     * @throws ClassCastException            if the class of an element of the specified
     *                                       collection prevents it from being added to this collection
     * @throws NullPointerException          if the specified collection contains a
     *                                       null element and this collection does not permit null elements,
     *                                       or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this
     *                                       collection
     * @throws IllegalStateException         if not, all the elements can be added at
     *                                       this time due to insertion restrictions
     * @see #add(NetworkNode)
     */
    @Override
    public boolean addAll(@NotNull Collection<? extends NetworkNode> c) {
        boolean output = false;

        for(NetworkNode node : c){
            if(AddNodeNoUpdate(node) == true){
                output = true;
            }
        }

        if(output == true){
            // finally, our ShortedPathRegistry is now out of date and will need to be recalculated.
            ShortestPathRegistry = new HashMap<>();

            PopulateShortestPathRegistry();
        }


        return output;
    }

    /**
     * Removes all of this collection's elements that are also contained in the
     * specified collection (optional operation).  After this call returns,
     * this collection will contain no elements in common with the specified
     * collection.
     *
     * @param c collection containing elements to be removed from this collection
     * @return {@code true} if this collection changed as a result of the
     * call
     * @throws UnsupportedOperationException if the {@code removeAll} method
     *                                       is not supported by this collection
     * @throws ClassCastException            if the types of one or more elements
     *                                       in this collection are incompatible with the specified
     *                                       collection
     *                                       ({@linkplain Collection##optional-restrictions optional})
     * @throws NullPointerException          if this collection contains one or more
     *                                       null elements and the specified collection does not support
     *                                       null elements
     *                                       ({@linkplain Collection##optional-restrictions optional})
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean output = false;

        for(Object o : c){
            if (!(o instanceof NetworkNode networkNode)) {
                return false;
            }

            RemoveNodeNoUpdate(networkNode);
        }

        if (output == true){
            ShortestPathRegistry.clear();
            PopulateShortestPathRegistry();
        }

        return output;
    }

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection (optional operation).  In other words, removes from
     * this collection all of its elements that are not contained in the
     * specified collection.
     *
     * @param c collection containing elements to be retained in this collection
     * @return {@code true} if this collection changed as a result of the call
     * @throws UnsupportedOperationException if the {@code retainAll} operation
     *                                       is not supported by this collection
     * @throws ClassCastException            if the types of one or more elements
     *                                       in this collection are incompatible with the specified
     *                                       collection
     *                                       ({@linkplain Collection##optional-restrictions optional})
     * @throws NullPointerException          if this collection contains one or more
     *                                       null elements and the specified collection does not permit null
     *                                       elements
     *                                       ({@linkplain Collection##optional-restrictions optional})
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean changed = false;

        for (NetworkNode node : Graph.vertexSet()) {

            if (!c.contains(node)) {
                if(RemoveNodeNoUpdate(node)){
                    changed = true;
                }

            }
        }

        if(changed == true){
            ShortestPathRegistry.clear();
            PopulateShortestPathRegistry();
        }

        return changed;
    }

    /**
     * Removes all the elements from this collection (optional operation).
     * The collection will be empty after this method returns.
     *
     * @throws UnsupportedOperationException if the {@code clear} operation
     *                                       is not supported by this collection
     */
    @Override
    public void clear() {
        ShortestPathRegistry.clear();
        Graph.removeAllVertices(Graph.vertexSet());
    }

    public Identifier getID(){
        return ID;
    }
}

