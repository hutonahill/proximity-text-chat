# Proximity Text Chat

## Goal
Minecraft's chat system has always seemed a little OP. 
You can send messages all over the world instantly with no cost from day one.

The goal of this mod is to give developers the tools to change this.

This mod will add a gamerule allowing you to disable the traditional text chat
in favor of a proximity based system with a configurable range. 

It also adds a networking system that will allow other mods to crate systems
that will allow players to add infrastructure to extend the range of their chats

We want to allow for the creation of systems that feel like your using regular
Minecraft chat, after you do the work to set it up.

The reason I am developing this mod is I would like to see it used by the 
[GalaxiesParzisStarWarsMod](https://github.com/Parzivail-Modding-Team/GalaxiesParzisStarWarsMod) project (PSWG).
I've written an outline of how I would use the features of this mod 
[here](https://docs.google.com/document/d/1DOEGuOlV2-_HNYUSr-h-6xP6oomEu9QLp1_bEtZ9RXU/edit?tab=t.0#heading=h.btip04f205p3).

This mod is not a full implementation of the linked design document, but the foundation for it. 


## ToDo List:

- Implement threading for populating the shortest path registries.
- revisit storage and make sure we are storing channels properly
- Nodes should have a set of receiving chunks instead of a single one (hutonahill)
- Item Tags:
  - sendInHand
  - sendInHotbar
  - sendInCurios
  - sendInInventory
  - receiveInHand
  - receiveInHotbar
  - receiveInCurios
  - receiveInInventory
  - channel
- Add ChatRangeMethod gamerule to the Mod page
- Add playerRange gamerule
- Add playerRange gamerule to the Mod page.


## License

This project is licensed under the GNU Lesser General Public License v3.0 (LGPL-3.0).

The LGPL allows you to:
- Use this code freely in your own projects, including proprietary ones, as long as you dynamically link to it.
- Modify and redistribute this code under the same LGPL license.
- Share improvements and changes to this code with the community.

For full details, see the [LICENSE](LICENSE) file.