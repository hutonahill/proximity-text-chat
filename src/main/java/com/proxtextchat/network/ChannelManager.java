package com.proxtextchat.network;

import com.proxtextchat.Message;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;


/**
 * manages all channels
 */
public class ChannelManager implements Collection<Channel>{

    /**
     * The singleton instance of the ChannelManager, providing access to the channel management system.
     * This instance is used to register, manage, and send messages between nodes within channels.
     */
    public static final ChannelManager Instance = new ChannelManager();

    private static final String ChannelListKey = "ChannelList";

    private static final HashMap<Identifier, Channel> Graphs = new HashMap<>();

    public static final String ChannelManagerFoler = "Channels";

    public static final String ChannelFile ="Channels.nbt";


    private ChannelManager(){}

    public void addChannels(Collection<Channel> channels){
        for(Channel channel : channels){



        }
    }

    /**
     * @param node Adds a node to the ChannelManager.
     *             Will automatically create a new channel if the node's channel is not registered with the ChannelManager.
     */
    public void addNode(@NotNull NetworkNode node){
        // check that we have the channel

        if (Graphs.containsKey(node.getChannelId())){
            Channel graph = Graphs.get(node.getChannelId());


            graph.add(node);

        }
        else{
            HashSet<NetworkNode> tempSet = new HashSet<>();
            tempSet.add(node);
            try {
                Graphs.put(node.getChannelId(), new Channel(tempSet));
            } catch (ChannelMismatch e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @param node Will remove the node from the channel if it was present.
     *             If the last node is removed from the channel, will remove the channel.
     */
    public void RemoveNode(@NotNull NetworkNode node){
        if(Graphs.containsKey((node.getChannelId()))){
            Channel graph = Graphs.get(node.getChannelId());
            
            graph.remove(node);

            if (graph.isEmpty()){
                Graphs.remove(node.getChannelId());
            }
        }
    }


    // stores subscriptions.
    // every node has a unique id, this allows storing a set of methods to be fired whenever we send a message.
    private static final HashMap<Integer, HashSet<Consumer<Message>>> NodeMessageEvent = new HashMap<>();

    /**
     * Retrieves a set of nodes that are receiving messages in the specified chunk from all channels.
     *
     * @param chunk the world chunk where the nodes are located.
     * @return a HashSet of nodes from all channels that receive at the specified chunk.
     * @throws RuntimeException if a ChannelMismatch occurs during processing.
     */
    public @NotNull HashSet<NetworkNode> NodesReceivingInChunk(@NotNull WorldChunk chunk){
        try {
            return NodesReceivingInChunk(chunk, Graphs.keySet());
        } catch (ChannelMismatch e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a set of nodes that are receiving messages in the specified chunk for a given set of channels.
     *
     * @param chunk the world chunk where the nodes are located.
     * @param channels a set of channels to search for nodes receiving messages.
     * @return a HashSet of nodes from the specified channels that are receiving at the given chunk.
     * @throws ChannelMismatch when a provided channel is not registered with the ChannelManager.
     */
    public @NotNull HashSet<NetworkNode> NodesReceivingInChunk(@NotNull WorldChunk chunk, @NotNull Set<Identifier> channels) throws ChannelMismatch {
        HashSet<NetworkNode> output = new HashSet<>();

        // loop though the listed channels.
        for(Identifier channel : channels){
            // make sure the current channel is registered.
            if(!Graphs.containsKey(channel)){
                throw new ChannelMismatch("Could not find channel " + channel.toString());
            }

            // get the nodes that receive for this channel.
            Map<ChunkReferance, HashSet<NetworkNode>> nodeReceivingRegistry = Graphs.get(channel).getNodeReceivingRegistry();

            // add those nodes that receive at chunk to the output set.
            if(nodeReceivingRegistry.containsKey(chunk)){
                output.addAll(nodeReceivingRegistry.get(chunk));
            }
        }

        return  output;
    }


    /**
     * Registers a player who will send messages to a specified channel.
     *
     * @param player the player to register.
     * @param channel the channel to register the player to.
     * @throws ChannelMismatch if the channel is not registered with the ChannelManager.
     */
    public void addReceivingFromPlayer(@NotNull PlayerEntity player, @NotNull Identifier channel) throws ChannelMismatch {
        if(Graphs.containsKey(channel)){
            Channel graph = Graphs.get(channel);

            graph.addReceivingFromPlayer(player);
        }
        else{
            throw new ChannelMismatch("No registered channel with that Identifier.");
        }
    }

    /**
     * Registers a player who will be sent messages from a specified channel.
     *
     * @param player the player to register.
     * @param channel the channel to register the player to.
     * @throws ChannelMismatch if the channel is not registered with the ChannelManager.
     */
    public void addSendToPlayer(@NotNull PlayerEntity player, @NotNull Identifier channel) throws ChannelMismatch {
        if(Graphs.containsKey(channel)){
            Channel graph = Graphs.get(channel);

            graph.addSendToPlayer(player);
        }
        else{
            throw new ChannelMismatch("No registered channel with that Identifier.");
        }
    }


    /**
     * Registers a player to receive messages from a collection of channels.
     *
     * @param player the player to register.
     * @param channels the collection of channels to register the player to.
     * @throws ChannelMismatch if any channel in the collection is not registered with the ChannelManager.
     */
    public void addReceivingFromPlayer(@NotNull PlayerEntity player, @NotNull Collection<Identifier> channels) throws ChannelMismatch{
        for(Identifier channel : channels){
            addReceivingFromPlayer(player, channel);
        }
    }

    /**
     * Registers a player to send messages to a collection of channels.
     *
     * @param player the player to register.
     * @param channels the collection of channels to register the player to.
     * @throws ChannelMismatch if any channel in the collection is not registered with the ChannelManager.
     */
    public void addSendToPlayer(@NotNull PlayerEntity player, @NotNull Collection<Identifier> channels) throws  ChannelMismatch{
        for(Identifier channel : channels){
            addSendToPlayer(player, channel);
        }
    }


    /**
     * Removes a player from receiving messages from all registered channels.
     *
     * @param player the player to remove.
     */
    public void removeReceivingFromPlayerEverywhere(@NotNull PlayerEntity player) {
        for(Identifier channel : Graphs.keySet()){
            Graphs.get(channel).removeReceivingFromPlayer(player);
        }
    }

    /**
     * Removes a player from sending messages to all registered channels.
     *
     * @param player the player to remove.
     */
    public void removeSendToPlayerEverywhere(@NotNull PlayerEntity player){
        for(Identifier channel : Graphs.keySet()){
            Graphs.get(channel).removeReceivingFromPlayer(player);
        }
    }


    /**
     * Removes a player from receiving messages from a specified channel.
     *
     * @param player the player to remove.
     * @param channel the channel to remove the player from.
     * @throws ChannelMismatch if the channel is not registered with the ChannelManager.
     */
    public void removeReceivingFromPlayer(@NotNull PlayerEntity player, @NotNull Identifier channel) throws ChannelMismatch{
        if(Graphs.containsKey(channel)){
            Channel graph = Graphs.get(channel);

            graph.removeReceivingFromPlayer(player);
        }
        else{
            throw new ChannelMismatch("No registered channel with that Identifier.");
        }
    }

    /**
     * Removes a player from sending messages to a specified channel.
     *
     * @param player the player to remove.
     * @param channel the channel to remove the player from.
     * @throws ChannelMismatch if the channel is not registered with the ChannelManager.
     */
    public void removeSendToPlayer(@NotNull PlayerEntity player, @NotNull Identifier channel) throws ChannelMismatch{
        if(Graphs.containsKey(channel)){
            Channel graph = Graphs.get(channel);

            graph.removeSendToPlayer(player);
        }
        else{
            throw new ChannelMismatch("No registered channel with that Identifier.");
        }
    }


    /**
     * Removes a player from receiving messages from a collection of channels.
     *
     * @param player the player to remove.
     * @param channels the collection of channels to remove the player from.
     * @throws ChannelMismatch if any channel in the collection is not registered with the ChannelManager.
     */
    public void removeReceivingFromPlayer(@NotNull PlayerEntity player, @NotNull Collection<Identifier> channels)  throws ChannelMismatch{
        for(Identifier channel : channels){
            removeReceivingFromPlayer(player, channel);
        }
    }

    /**
     * Removes a player from sending messages to a collection of channels.
     *
     * @param player the player to remove.
     * @param channels the collection of channels to remove the player from.
     * @throws ChannelMismatch if any channel in the collection is not registered with the ChannelManager.
     */
    public void removeSendTOPlayer(@NotNull PlayerEntity player, @NotNull Collection<Identifier> channels) throws ChannelMismatch{
        for(Identifier channel : channels){
            removeSendToPlayer(player, channel);
        }
    }


    /**
     * Retrieves the set of channels from which a player is registered to receive messages.
     *
     * @param player the player whose receiving channels are to be retrieved.
     * @return a set of channel identifiers representing the channels the player is receiving from.
     */
    public @NotNull HashSet<Identifier> getReceivingFromChannelsForPlayer(@NotNull PlayerEntity player){
        HashSet<Identifier> output = new HashSet<>();
        for(Identifier channel : Graphs.keySet()){
            if(Graphs.get(channel).hasReceivingFromPlayer(player)){
                output.add(channel);
            }
        }

        return output;
    }

    /**
     * Retrieves the set of channels to which a player is registered to send messages.
     *
     * @param player the player whose sending channels are to be retrieved.
     * @return a set of channel identifiers representing the channels the player is sending to.
     */
    public @NotNull HashSet<Identifier> getSendToChannelsForPlayer(@NotNull PlayerEntity player){
        HashSet<Identifier> output = new HashSet<>();
        for(Identifier channel : Graphs.keySet()){
            if(Graphs.get(channel).hasSendToPlayer(player)){
                output.add(channel);
            }
        }

        return output;
    }

    public Set<Channel> getChannelSet(){
        return Set.copyOf(Graphs.values());
    }


    /**
     * Subscribes a method to receive messages for a specific network node.
     * This method adds the provided consumer to the registry,
     * allowing it to be triggered when messages are sent to the node.
     * Designed for non-players.
     *
     * @param node the network node to subscribe to.
     * @param method the method to be invoked when a message is received by the node.
     */
    public static void SubscribeToNodeMessage(@NotNull NetworkNode node, @NotNull Consumer<Message> method){
        // if there is no set in this slot, add one to avoid a nullptr
        if (!NodeMessageEvent.containsKey(node.getID())){
            NodeMessageEvent.put(node.getID(), new HashSet<>());
        }

        NodeMessageEvent.get(node.getID()).add(method);
    }

    /**
     * Unsubscribes a method from receiving messages for a specific network node.
     * This method removes the provided consumer from the registry,
     * so it will no longer be triggered when messages are sent to the node.
     * If there are no more subscribed methods for a node, it will be removed from the registry for efficiency.
     *
     * @param node the network node to unsubscribe from.
     * @param method the method to be removed from the subscription list.
     */
    public static void UnsubscribeToNodeMessage(@NotNull NetworkNode node, @NotNull Consumer<Message> method){
        if(NodeMessageEvent.containsKey(node.getID())){
            NodeMessageEvent.get(node.getID()).remove(method);
            // if a slot is empty, remove it from the registry to keep our trigger maximally efficient.
            if(NodeMessageEvent.get(node.getID()).isEmpty()){
                NodeMessageEvent.remove(node.getID());
            }
        }
    }


    /**
     * Sends a direct message from a source node to a destination node, provided both nodes are in the same channel.
     *
     * @param source the source node from which the message is sent.
     * @param destination the destination node to which the message is sent.
     * @param message the message to be sent.
     * @return {@code true} if the message was successfully delivered;
     * {@code false} if no valid path exists between the nodes.
     * @throws ChannelMismatch if the source and destination nodes are not in the same channel.
     * @throws IllegalArgumentException if the source node's channel is not registered with the ChannelManager or
     * the source/destination node is not found in the channel.
     */
    public static boolean directMessage(@NotNull NetworkNode source, @NotNull NetworkNode destination, @NotNull Message message) throws ChannelMismatch {
        if (source.getChannelId() != destination.getChannelId()){
            throw new ChannelMismatch("Nodes must have the same channel to send messages between them.");
        }

        if(!Graphs.containsKey(source.getChannelId())){
            throw new IllegalArgumentException("Channel not registered.");
        }

        Channel graph = Graphs.get(source.getChannelId());

        if (graph.contains(source)){
            throw new IllegalArgumentException("source node not found.");
        }

        if (graph.contains(destination)){
            throw new IllegalArgumentException("destination node not found.");
        }

        ArrayList<NetworkNode> path = graph.DirectMessage(source, destination);
        
        if(path != null){
            message.AddTrace(path);

            TriggerNodeMessage(destination, message);
            
            return true;
        }
        
        // when there is no valid path from source to destination.
        return false;
    }

    /**
     * Broadcasts a message from a source node to all nodes in the same channel that it is connected to.
     *
     * @param source the source node from which the message is broadcast.
     * @param message the message to broadcast.
     * @throws IllegalArgumentException if the source node's channel is not registered with the ChannelManager or
     * the source node is not found in the channel.
     */
    public void broadcastMessage(@NotNull NetworkNode source, Message message){
        if(!Graphs.containsKey(source.getChannelId())){
            throw new IllegalArgumentException("Channel not registered.");
        }

        Channel graph = Graphs.get(source.getChannelId());

        if (graph.contains(source)){
            throw new IllegalArgumentException("source node not found.");
        }

        HashMap<NetworkNode, ArrayList<NetworkNode>> paths = graph.BroadcastPaths(source);

        // we send a message to each node the source is connected to.
        for (NetworkNode destination : paths.keySet()){
            Message newMessage = new Message(message.getTrueSender(), message.getAlias(), message.getMessage());

            newMessage.AddTrace(paths.get(destination));

            TriggerNodeMessage(destination, newMessage);
        }
    }


    private static void TriggerNodeMessage(@NotNull NetworkNode node, @NotNull Message message){
        if(NodeMessageEvent.containsKey(node.getID())){
            for (Consumer<Message> method : NodeMessageEvent.get(node.getID())){
                method.accept(message);
            }
        }

        // now deliver the messages to players

        Set<PlayerEntity> registeredPlayers = Graphs.get(node.getChannelId()).getSendToPlayerRegistry();

        for(PlayerEntity player : registeredPlayers){
            // filter out offline players
            World world = player.getWorld();
            if (world != null && player.getServer() != null) {

                // Get the player's current chunk coordinates
                int chunkX = player.getBlockPos().getX();
                int chunkZ = player.getBlockPos().getZ();

                // Retrieve the chunk from the world
                WorldChunk playerChunk = world.getChunk(chunkX, chunkZ);

                if(node.getRangeChunks().contains(playerChunk)){
                    player.sendMessage(message.getMessage());
                }
            }
        }
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
        return Graphs.size();
    }

    /**
     * Returns {@code true} if this collection contains no elements.
     *
     * @return {@code true} if this collection contains no elements
     */
    @Override
    public boolean isEmpty() {
        return Graphs.isEmpty();
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
        return Graphs.containsValue(o);
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
    public Iterator<Channel> iterator() {
        return Graphs.values().iterator();
    }

    /**
     * Returns an array containing all of the elements in this collection.
     * If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order. The returned array's {@linkplain Class#getComponentType
     * runtime component type} is {@code Object}.
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this collection.  (In other words, this method must
     * allocate a new array even if this collection is backed by an array).
     * The caller is thus free to modify the returned array.
     *
     * @return an array, whose {@linkplain Class#getComponentType runtime component
     * type} is {@code Object}, containing all of the elements in this collection
     * @apiNote This method acts as a bridge between array-based and collection-based APIs.
     * It returns an array whose runtime type is {@code Object[]}.
     * Use {@link #toArray(Object[]) toArray(T[])} to reuse an existing
     * array, or use {@link #toArray(IntFunction)} to control the runtime type
     * of the array.
     */
    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return Graphs.values().toArray();
    }

    /**
     * Returns an array containing all of the elements in this collection;
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
     * @return an array containing all of the elements in this collection
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
    public <T> T[] toArray(@NotNull T[] a) {
        return Graphs.values().toArray(a);
    }

    /**
     * Ensures that this collection contains the specified element (optional
     * operation).  Returns {@code true} if this collection changed as a
     * result of the call.  (Returns {@code false} if this collection does
     * not permit duplicates and already contains the specified element.)<p>
     * <p>
     * Collections that support this operation may place limitations on what
     * elements may be added to this collection.  In particular, some
     * collections will refuse to add {@code null} elements, and others will
     * impose restrictions on the type of elements that may be added.
     * Collection classes should clearly specify in their documentation any
     * restrictions on what elements may be added.<p>
     * <p>
     * If a collection refuses to add a particular element for any reason
     * other than that it already contains the element, it <i>must</i> throw
     * an exception (rather than returning {@code false}).  This preserves
     * the invariant that a collection always contains the specified element
     * after this call returns.
     *
     * @param channel element whose presence in this collection is to be ensured
     * @return {@code true} if this collection changed as a result of the
     * call
     * @throws UnsupportedOperationException if the {@code add} operation
     *                                       is not supported by this collection
     * @throws ClassCastException            if the class of the specified element
     *                                       prevents it from being added to this collection
     * @throws NullPointerException          if the specified element is null and this
     *                                       collection does not permit null elements
     * @throws IllegalArgumentException      if some property of the element
     *                                       prevents it from being added to this collection
     * @throws IllegalStateException         if the element cannot be added at this
     *                                       time due to insertion restrictions
     */
    @Override
    public boolean add(Channel channel) {
        if(Graphs.containsKey(channel.getID())){

            Graphs.get(channel.getID()).MergeChannels(channel);
        }
        else{
            Graphs.put(channel.getID(), channel);
        }

        return true;
    }

    /**
     * Removes a single instance of the specified element from this
     * collection, if it is present (optional operation).  More formally,
     * removes an element {@code e} such that
     * {@code Objects.equals(o, e)}, if
     * this collection contains one or more such elements.  Returns
     * {@code true} if this collection contained the specified element (or
     * equivalently, if this collection changed as a result of the call).
     *
     * @param o element to be removed from this collection, if present
     * @return {@code true} if an element was removed as a result of this call
     * @throws ClassCastException            if the type of the specified element
     *                                       is incompatible with this collection
     *                                       ({@linkplain Collection##optional-restrictions optional})
     * @throws NullPointerException          if the specified element is null and this
     *                                       collection does not permit null elements
     *                                       ({@linkplain Collection##optional-restrictions optional})
     * @throws UnsupportedOperationException if the {@code remove} operation
     *                                       is not supported by this collection
     */
    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Channel channel)) {
            return false; // Return false if not a Channel
        }

        // If the channel exists, remove it using its Identifier key
        Identifier id = channel.getID();
        return Graphs.remove(id, channel); // Removes only if the value matches
    }


    /**
     * Returns {@code true} if this collection contains all of the elements
     * in the specified collection.
     *
     * @param c collection to be checked for containment in this collection
     * @return {@code true} if this collection contains all of the elements
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
        return Graphs.values().containsAll(c);
    }

    /**
     * Adds all of the elements in the specified collection to this collection
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
     * @throws IllegalStateException         if not all the elements can be added at
     *                                       this time due to insertion restrictions
     * @see #add(Channel)
     */
    @Override
    public boolean addAll(@NotNull Collection<? extends Channel> c) {
        for(Channel channel : c){
            add(channel);
        }
        return true;
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
        for(Object o : c){
            remove(o);
        }

        return true;
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

        for (Identifier id : Graphs.keySet()) {
            Channel channel = Graphs.get(id);

            if (channel != null && !c.contains(channel)) {
                Graphs.remove(id);
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     * The collection will be empty after this method returns.
     *
     * @throws UnsupportedOperationException if the {@code clear} operation
     *                                       is not supported by this collection
     */
    @Override
    public void clear() {
        Graphs.clear();
    }
}