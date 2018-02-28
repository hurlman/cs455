=================================
Mike Hurley
02/28/2018
CS 455 - Homework 2 - Programming Component
=================================

I created a class to kick off several client threads, as my client implements runnable.  This class
is called MultiClient.  The number of clients to run is the 4th command line argument. Since I am remote
and it's easy to just create 10 ssh sessions with SuperPutty, this was the easiest way for me to
create 100 client sessions.  The standard Client class can still be used, as well.
MultiClient is used as such:
java cs455.scaling.client.MultiClient <ServerIP> <ServerPort> <MessageRate> <NumberOfClients>

New client jobs can be stopped and started without restarting server.  The server
is able to clean up and close old client connections.

There is no exit command for the client or server, so CTRL+C is used.

In stress testing, I was able to max out my server at roughly 1400 messages/s, with some combination
of client connections and message rate.  Beyond that the clients began receiving invalid hashes.  As
this is already well beyond what is described in the assignment, I stopped troubleshooting why the
invalid hashes were happening.  My hunch is buffers were getting full, but I'm not certain.

==================================
File List
==================================
cs455/scaling/client/Client.java
    Client class, connects to server, sends random data, tracks hash responses.

cs455/scaling/client/MultiClient.java
    Class that can spin up multiple clients in separate threads on one machine.

cs455/scaling/server/ClientConnection.java
    Class used by server to store buffer for each client, track messages sent, and used to lock channel
    to a thread.

cs455/scaling/server/Server.java
    Server class, handles over 100 client connections and responds with 20 byte SHA1 hashes of
    the data that is sent by the clients.

cs455/scaling/tasks/ChannelWorker.java
    Abstraction of the task performed by the server every time a client channel is read.  Reads
    data from the buffer, calculates the hash and writes back to the buffer.  Implements the
    IClientTask interface.

cs455/scaling/tasks/IClientTask.java
    Interface that can be processed by the ThreadPoolManager.

cs455/scaling/thread/ThreadPoolManager.java
    Custom implementation of a thread pool that executes tasks of type IClientTask.  Uses a LinkedList
    data structure to queue tasks to execute.  Worker threads are an inner class, and block on the task
    queue.

cs455/scaling/util/Util.java
    Utility functions and constants.  Defines report interval, buffer sizes, data size.
    SHA1 hash functions calculate hashes in both hex string and byte[] format.

makefile
    make clean and make all.
