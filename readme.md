# Rejewled #

## History ##
FamilyJewels was a plugin for Bukkit that provides a level of protectection against X-Ray cheaters, while not sacraficing efficency. Unlike other anti-xray plugins FamilyJewels strived to provide an anticheat experience that does not impact a legitimate player in any way. Someone who is playing should never know that FamilyJewels is installed. 

However, FamilyJewels has been abandoned since minecraft 1.2, and much of the minecraft server internals have changed during that time. But I have put in the effort to upgrade and rewrite the code, because I believe in its mission.

## How it works ##
In order to provide minimal intrusion for regular players, Rejewled only hides blocks that are not directly surrounded by air. 

To do this, Rejewled will hook the players connection to rewrite the packets sent to the player. As a result of this hook, this plugin is mosty likely incompatible with other plugins that also do this (eg Spigot)

Whenever a player punches or breaks a block, the plugin will then update the client to show any nearby ores. This is very effective as it uncovers ores only when a player is nearby and digging. 

##Issues and Feedback##
Please report all issues and feedback with Rejewled in the Github issue tracker, on the right sidebar. 

Please keep in mind that this is not a pure Bukkit plugin and this will only work on pure CraftBukkit servers. In addition, because of protections built into CraftBukkit, you must always update this plugin after a patch. Versions built for older CB versions **will not run**. Since this plugin hooks into the internals of CraftBukkit, there is always the possibility of world corruption or otherwise weird behavior. Use this plugin on a test server before deploying it to a production enviroment.

While we personally have been running this plugin for many months, we cannot guarntee that it won't break your world. Please make backups.

##Building##
Building this plugin requires downloading and building CraftBukkit seperately, after slightly modifying its pom file. This is ONLY a requirement for building the plugin, the plugin **will** run on stock CraftBukkit.

The patch that needs to be applied to CraftBukkit's pom.xml is:
````patch
5c5
<   <artifactId>craftbukkit-nr</artifactId>
---
>   <artifactId>craftbukkit</artifactId>
260c260
<                 <!--<relocation>
---
>                 <relocation>
270c270
<                 </relocation>-->
---
>                 </relocation>
````

Just go through the file, and for every line with a `>` next to it, replace it with the line above.
