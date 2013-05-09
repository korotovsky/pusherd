package com.korotovsky.server.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientSocket implements Runnable {
    final private static String EXIT_SIGNAL = "exit";
    private Socket socket;
    private Logger logger;
    private PrintWriter writer;
    private BufferedReader reader;

    /**
     * @param logger Logger
     */
    public ClientSocket(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            writer = new PrintWriter(this.socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                logger.info("Trying to read data");

                read();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public boolean read() throws IOException {
        String line = reader.readLine();

        logger.info("Remote socket address: " + socket.getRemoteSocketAddress().toString());
        logger.info("Received data: " + line);

        if (EXIT_SIGNAL.equals(line)) {
            logger.info("Remote socket has gone away: " + socket.getRemoteSocketAddress().toString());

            return close();
        }

        return true;
    }

    /**
     * @param socket Socket
     * @return ClientSocket
     */
    public ClientSocket setSocket(Socket socket) {
        this.socket = socket;

        return this;
    }

    public boolean close() throws IOException {
        writer.close();
        reader.close();
        socket.close();

        return true;
    }
}