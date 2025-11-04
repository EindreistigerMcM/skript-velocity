# Skript Velocity 
## What can you do with Skript Velocity?
Like in the name Skript Velocity handles Velocity stuff.

- north, west, east, south, up, down
- left, right, forward, backward
- away from, towards

This Plugin supports all 1.21.x version.

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
` set velocity of %entity% towards (%entity%|%location%) at speed %number%`

Example Item that will Pull Mobs towards the player with a feather:
```
on right click with a feather:
    if player's tool's name is "Pull Mobs":
        loop all entities in radius 10 of player:
            set velocity of loop-entity towards player at speed 3
```
If you want to use locations you can use `location(x, y, z)´ or a variable
 
 ### Away From
` set velocity of %entity% away from (%entity%|%location%) at speed %number%`

Example Item that will Push Mobs Away from the Player with a feather:
```
on right click with a feather:
    if player's tool's name is "Push Mobs Away":
        loop all entities in radius 10 of player:
            set velocity of loop-entity away from player at speed 3
```
If you want to use locations you can use `location(x, y, z)´ or a variable

### Vector
`set velocity [of] %entity% to vector %number%, %number%, %number% [at speed %number%]`

Example command that pushes the player upwards using Vector:
```
command /vector:
    trigger:
        set velocity of player to vector 0, 1, 0 at speed 1
```

## Suggestions?
Make an issue here if you have a suggestion or any issues with this Addon. Thank you!

## [Latest Release 1.0.3](https://github.com/EindreistigerMcM/skript-velocity/releases/download/1.0.3/skript-velo-1.0.3.jar)
