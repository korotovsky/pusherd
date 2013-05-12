Pusherd - a simple Java game server
=======

This is a simple game server, written in Java, without using third-party libraries.

Will be lister connections on 127.0.0.1 and 10001 port.

TODO: cli implementation for the server

TODO: GUI client url

Description
===
The goal of the game - who clicks the button 100/200/etc times faster.
There may be a lot of players, when all set the flag that they are ready to play,
players about this will be notified and begin game. As soon as one of them reaches the limit, 
all players will also be notified of this behavior.
The game ends.

Build status: [![Build Status](https://travis-ci.org/korotovsky/pusherd.png?branch=master)](https://travis-ci.org/korotovsky/pusherd)
