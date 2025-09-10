* Now all configurations and storages work in the main thread, and writing in an asynchronous one, which should remove inconsistencies and not load the server with a new timer.
* Now to add points to the key using the command, you can use the nickname of the offline player.
* Now there cannot be two identical timers (two timer rewards in one task for one goal).