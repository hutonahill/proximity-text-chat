# Proximity Text Chat

## Goal
Minecraft's chat system has always seemed a little overpowered. 
You can send messages all over the world instantly with no cost from day one.

The goal of this mod is to give developers the tools to change this.

This mod will add a gamerule allowing you to disable the traditional text chat
in favor of a proximity-based system with a configurable range. 

It also adds a networking system that will allow other mods to crate systems
that will allow players to add infrastructure to extend the range of their chats

We want to allow for the creation of systems that feel like you're using regular
Minecraft chat, after you do the work to set it up.

The reason I am developing this mod is I would like to see it used by the 
[GalaxiesParzisStarWarsMod](https://github.com/Parzivail-Modding-Team/GalaxiesParzisStarWarsMod) project (PSWG).
I've written an outline of how I would use the features of this mod 
[here](https://docs.google.com/document/d/1DOEGuOlV2-_HNYUSr-h-6xP6oomEu9QLp1_bEtZ9RXU/edit?tab=t.0#heading=h.btip04f205p3).

This mod is not a full implementation of the linked design document, but the foundation for it. 


## ToDo List:

### Minimum Viable Product
- Done!

### Phase Two
- Add commands:
  - `/AddNode <channelIdentifier> <setOfReceaveChunks> <setOfRangeChunks>` 
    - Returns node ID
  - `/RemoveNode <Id> <optionalChannelIdentifier>`
    - removes a node
  - `/DirectMessage <sourceNodeId> <destinationNodeId> <quotedMessageContent> <optionalAlius>`
    - Sends a message from one node to another
  - `/BraoudcastMessage <sourceNodeId> <quotedMessageContent> <optionalAlius>`
    - sends a message to all nodes connected to the source node.
  - `/GetNodeList <optionalChannelIdentifier>` 
    - returns a list of nodes and the chunks they cover, optionally restricted to a channel.
- Implement threading for populating the shortest path registries.
- Add documentation explaining how to use this stuff.

### Possible Additional Features
- Item Tags:
  - sendInCurios
  - receiveInCurios
- Add compatability with proximity voice chat mod.
- Some way to Visually see the chunks within a node.
- Support for Xero's World Map and viewing node channel ranges
- Support for Journey Map and viewing node channel ranges


## Documentation
This mod adds a system of networks with nodes (channels) A node is composed of a set of `CunkReferanc`s 
which it can send messages to and a set of `CunkReferanc`s it can receive messages from. 
This creates a network you can send messages though using methods in `ChannelManager`. 
Messages contain the text of the message the actual sending entity, an alias for that entity and 
the path the message took to get to its destination.

This section will be rewritten as the mod gets closer to completion.

## Versioning
The version number of this mod `<minecraft_version>-<mod_version>`. 
`mod_version` will continue to iterate independent of `minecraft_version`

## Testing
Wow, I really hate coming up with tests.
I'm having included any tests here,
but I am definitely willing to include them so long as i don't have to come up with them.
If you have an idea for a test open an issue and if I think its valid, I will include it.
The proposal should include inputs and expected outputs.

## License
This project is licensed under the GNU Lesser General Public License v3.0 (LGPL-3.0).

The LGPL allows you to:
- Use this code freely in your own projects, including proprietary ones, as long as you 
dynamically link to it.
- Modify and redistribute this code under the same LGPL license.
- Share improvements and changes to this code with the community.

For full details, see the [LICENSE](LICENSE) file.

## Structure
We generated the file structure using [this](https://fabricmc.net/develop/template/) 
tool.
We are using it under the CC0 license.

## Thanks
Many thanks to DeltaHelios, Parzi, and many people on the fabric discord for helping me 
develop my first mod! There is no way I could do this alone!

## Activity
![Alt](https://repobeats.axiom.co/api/embed/3c3df086f5a4a4b42466dd97000fdc6b639b3c7f.svg "Repobeats analytics image")