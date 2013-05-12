package com.korotovsky.pusherd.network;

import com.korotovsky.pusherd.GameServer;
import com.korotovsky.pusherd.client.*;
import com.korotovsky.pusherd.core.PlayerGame;
import com.korotovsky.pusherd.events.PlayerEvents;
import com.korotovsky.pusherd.network.protocol.*;
import com.korotovsky.pusherd.network.protocol.responses.system.AcceptedResponse;
import com.korotovsky.pusherd.network.protocol.responses.system.ErrorResponse;
import com.korotovsky.pusherd.network.protocol.responses.system.MessageResponse;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientSocket
        implements Runnable, PlayerEvents {

    public final static Integer SUPPORTED_CLIENT_VERSION = 1;

    private Boolean isRegistered = false;

    private GameServer gameServer;
    private Logger logger;
    private Socket socket;

    private BufferedWriter writer;
    private BufferedReader reader;

    public ClientSocket(GameServer gameServer, Socket socket) {
        this.socket = socket;
        this.gameServer = gameServer;
        this.logger = gameServer.getLogger();
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            logger.info(e.getMessage());
        }

        while (true) {
            try {
                read();
            } catch (Throwable e) {
                logger.info(e.getMessage());
                break;
            }
        }
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public Boolean getIsRegistered() {
        return isRegistered;
    }

    /**
     * Callback for simple echo message
     *
     * @param request Request
     * @throws IOException
     */
    public void onEchoMessage(Request request) throws IOException {
        new MessageResponse(writer).setMessage(request.getLine()).send();
    }

    /**
     * Callback for close all readers and writers
     *
     * @param request Request
     * @throws Throwable
     */
    public void onCloseConnection(Request request) throws Throwable {
        Player player = gameServer.getPlayers().get(this);

        gameServer.onRemoveClient(this);
        gameServer.getGame().onPlayerDisconnected(this, player);

        writer.close();
        reader.close();
        socket.close();
    }

    /**
     * Callback for handshake request: helo:PROTOCOL:COMPUTER_NAME
     *
     * @param request Request
     * @throws IOException
     */
    public void onHandshake(Request request) throws IOException {
        String[] parts = request.getLine().split(":");

        if (parts.length < 2) {
            new ErrorResponse(writer).setMessage(ErrorResponse.MSG_INVALID_HELO).send();
        }

        Integer version = Integer.parseInt(parts[1]);
        String name = parts[2];

        if (version.equals(SUPPORTED_CLIENT_VERSION)) {
            Player player = new Player(new PlayerGame());
            player.setVersion(version);
            player.setName(name);

            try {
                gameServer.onPutClient(this, player);
                gameServer.getGame().onPlayerConnected(this, player);

                new AcceptedResponse(writer).send();

                isRegistered = true;
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        } else {
            new ErrorResponse(writer).setMessage(ErrorResponse.MSG_MISMATCH_VERSION).send();
        }
    }

    private void read() throws Throwable {
        Request request = new Request(gameServer.getGame(), reader, this);

        request.dispatch();
    }
}