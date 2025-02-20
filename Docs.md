# How to Use This Library
The intention of this library is for developers to interact with the
[ChannelManager](src%2Fmain%2Fjava%2Fcom%2Fproxtextchat%2Fnetwork%2FChannelManager.java).
The channel manager provides public methods to create new nodes, to send a message. 
However, there are others.

### [NetworkNode](src%2Fmain%2Fjava%2Fcom%2Fproxtextchat%2Fnetwork%2FNetworkNode.java)
This is the most fundamental piece of the network. It is composed of three parts:
 - A set of chunks where the node can receive messages
 - A set of chunks to which the node can receive messages.
 - A channel Identifier.

And that's pretty much of it. Network Node doesn't do any computation,
its just a place to store data and a vague location. 

### [Channel](src%2Fmain%2Fjava%2Fcom%2Fproxtextchat%2Fnetwork%2FChannel.java)
A channel is a collection of NetworkNode's. Its network. It's composed of 
a Graph of Network Nodes and the connections between them. Its job is to 
determine what nodes should be connected by looking at sending and receiving chunk sets.
It also maintains a table of the shortest path from every node to every other node 
measured by number of jumps. Maintaining this table is extremely computationally
expensive and due to how the network work it must totally rebuild this table on 
start of and whenever a node is added or removed. Therefore, developers should be careful
about how frequently they modify networks and make use of the `addAll` and `removeAll`
methods which only recalculate once.

Channels also handle sending messages. They receive a message, trace its path though the network
and deliver the message to its endpoint. It has methods allowing players to be added
to a registry of players who can receive messages sent on this channel. Players will
only receive messages if they are within a chunk that the channel can broadcast to.

Channels also contain a registry of players who can send messages to nodes. Players in this
registry will only be able to send messages to nodes if they are within a 
receiving chunk of that node.

### [ChannelManager](src%2Fmain%2Fjava%2Fcom%2Fproxtextchat%2Fnetwork%2FChannelManager.java)
This is the main class I expect developers to interact with. It has many methods whose job is to
trigger features of the Channel class. It also contains a system allowing systems to subscribe nodes, 
receiving messages they receive. This system allows other entities to adopt a node, receiving 
messages from it and sending messages as it.

The goal of this class is that it should be able to do anything you want to do with the collection
of networks. add nodes, it automatically adds a channels, it removes unused channels, it allows for 
players to subscribe to channels and it allows systems to subscribe to channels. It also has methods 
facilitate sending messages.

### [ChannelMismatch](src%2Fmain%2Fjava%2Fcom%2Fproxtextchat%2Fnetwork%2FChannelMismatch.java)
Channel Mismatch is a custom exception. It deals with situations where a system 
expects one channel but gets another. This most commonly occurs when adding nodes in bulk and
some of those nodes that don't have matching channels.