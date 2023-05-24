## Simple Text Adeventure Game Assignment

The construction of a general-purpose socket-server game-engine for text adventure games.

## Task Outline

The aim is to build a game engine server that communicates with one or more game clients.
The server will listen for incoming connections from clients.
When a connection has been made, the server will receive an incoming command, process the actions
that have been requested, make changes to any game state that is required and then finally send back a
suitable response to the client.

The basic networking operation was provided for this project.

To execute the server from the command line, type `mvnw exec:java@server`.
To execute the client that will connect to the server, type `mvnw exec:java@client -Dexec.args="player"`
note that the `-Dexec.args` flag allows us to pass an argument into the client
(in this case the username of the current player).
This username is then passed by the client to the server at the start of each command
(so the server knows which player the command came from).

The aim of the assignment was to build a versatile game engine that it is able to play _any_ text adventure game
(providing that it conforms to certain rules). To support this versatility, two configuration files:
**entities** and **actions** are used to describe the various "things" that are present in the game,
their structural layout and dynamic behaviours.
These two configuration files are passed into the game server when it is instantiated like so:

``` java
public GameServer(File entitiesFile, File actionsFile)
```

The server will load the game scenario in from the two configuration files, thus allowing a range of different games to be played.

The server is robust and resilient, able to keep running no matter what commands the user throws at it. That game state must NOT be made persistent between server invocations.
When the server starts up, the game state should be loaded from the _original_ config files. The server should NOT remember the state of any previous games.

## Commands

There are a number of standard "built-in" gameplay commands that your game engine should respond to:

- "inventory" (or "inv" for short): lists all of the artefacts currently being carried by the player
- "get": picks up a specified artefact from the current location and adds it into player's inventory
- "drop": puts down an artefact from player's inventory and places it into the current location
- "goto": moves the player to the specified location (if there is a path to that location)
- "look": prints names and descriptions of entities in the current location and lists paths to other locations

In addition to these standard "built-in" commands, it is possible to customise a game with a number of additional **actions**.

## Actions

In addition to the standard "built-in" commands (e.g. `get`, `goto`, `look` etc.), your game engine should also
respond to any of a number of game-specific commands (as specified in the "actions" file).
Each of these **actions** will have the following elements:

- One or more possible **trigger** phrases (ANY of which can be used to initiate the action)
- One or more **subject** entities that are acted upon (ALL of which need to be available to perform the action)
- An optional set of **consumed** entities that are all removed ("eaten up") by the action
- An optional set of **produced** entities that are all created ("generated") by the action
- A **narration** that provides a human-readable explanation of what happened when the action is performed

Action trigger keyphrases are NOT unique - for example there may be multiple "open" actions that
act on different entities. 

## Entities

Entities represent a range of different "things" that exist within a game.
The different types of entity represented in the game are as follows:

- Locations: Rooms or places within the game
- Artefacts: Physical things within the game that can be collected by the player
- Furniture: Physical things that are an integral part of a location
(these can NOT be collected by the player)
- Characters: The various creatures or people involved in game
- Players: A special kind of character that represents the user in the game

Locations are complex constructs in their own right and have various different attributes including:
- Paths to other locations (note: it is possible for paths to be one-way !)
- Characters that are currently at a location
- Artefacts that are currently present in a location
- Furniture that belongs within a location

Entities are defined in one of the game configuration files using a language called "DOT".
