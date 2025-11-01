# Sourcing Aliens and Lasers: Event-Sourced Space Invaders

This is an adaptation of the Java Space Invaders game clone made by Jan Bodnar.

This adaptation demonstrates how to use Event Sourcing in a simple and fun way.
It also shows how to use a DCB Event Store and how to replay events for time travel.

Space Invaders is an arcade video game designed by Tomohiro Nishikado. It was first released in 1978.

In Space Invaders game, the player controls a cannon. He is about to save the Earth from invasion of evil space invaders.

In our Java clone we have 24 invaders. These aliens heavily shell the ground. When the player shoots a missile, he can shoot another one only when it hits an alien or the top of the Board. The player shoots with the Space key. Aliens launch randomly their bombs. Each alien shoots a bomb only after the previous one hits the bottom.

A description of the original game can be found at:
https://zetcode.com/javagames/spaceinvaders/

![Space Invaders screenshot](spaceinvaders.png)

