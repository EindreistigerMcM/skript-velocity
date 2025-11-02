# Skript Velocity 
## What can you do with Skript Velocity?
Like in the name Skript Velocity handles Velocity stuff. Currently there is only the Directions as in

- north, west, east, south, up, down
- left, right, forward, backward
- away from, towards

### Example

```
on right click with a feather:
    if player's tool's name is "Push Mobs Away":
        loop all entities in radius 10 of player:
            set velocity of loop-entity away from player at speed 3
    else if player's tool's name is "Pull Mobs":
        loop all entities in radius 10 of player:
            set velocity of loop-entity towards player at speed 3
```

## Structured

If you want to write your own code you need the docs, for now you the only docs are in here:

### Directions
`set velocity [of] %entity% [to] (north|south|east|west|up|down|forward|backward|left|right) at speed %number%`

Example Command to Push the player upwards:
```
command /launch:
    trigger:
        set velocity of player to up at speed 2
```
### Towards
` set velocity [of] %entity% towards %entity% at speed %number%`

Example Item that will Pull Mobs towards the player with a feather:
```
on right click with a feather:
    if player's tool's name is "Pull Mobs":
        loop all entities in radius 10 of player:
            set velocity of loop-entity towards player at speed 3
```
 
 ### Away From
` set velocity [of] %entity% away from %entity% at speed %number%`

Example Item that will Push Mobs Away from the Player with a feather:
```
on right click with a feather:
    if player's tool's name is "Push Mobs Away":
        loop all entities in radius 10 of player:
            set velocity of loop-entity away from player at speed 3
```
## [Latest Release 1.0.0](https://github.com/EindreistigerMcM/skript-velocity/releases/download/release/skript-velo-1.0.jar)
