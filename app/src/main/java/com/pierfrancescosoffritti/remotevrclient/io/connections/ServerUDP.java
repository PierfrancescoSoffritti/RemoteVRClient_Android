package com.pierfrancescosoffritti.remotevrclient.io.connections;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.pierfrancescosoffritti.remotevrclient.EventBus;
import com.pierfrancescosoffritti.remotevrclient.Events;
import com.pierfrancescosoffritti.remotevrclient.io.data.GameInput;
import com.pierfrancescosoffritti.remotevrclient.logging.LoggerBus;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by  Pierfrancesco on 05/05/2016.
 */
public class ServerUDP implements ServerIO {

    protected final String LOG_TAG = getClass().getSimpleName();

    private final static int SOCKET_TIMEOUT = 2000;

    private SocketAddress socketAddress;

    private DatagramSocket socket;
    private DatagramPacket inputPacket;
    private DatagramPacket outputPacket;

    public ServerUDP(String serverIP, int communicationPort, int initConnectionPort) throws  IOException {
        socket = new DatagramSocket();
        socket.setSoTimeout(SOCKET_TIMEOUT);

        socketAddress = new InetSocketAddress(InetAddress.getByName(serverIP), communicationPort);
        outputPacket = new DatagramPacket(new byte[GameInput.BUFFER_SIZE], GameInput.BUFFER_SIZE, socketAddress);
        inputPacket = new DatagramPacket(new byte[100000], 100000, socketAddress);

        EventBus.getInstance().post(new Events.ServerConnecting());

        initConnection(serverIP, initConnectionPort);
    }

    private void initConnection(String serverIP, int serverPort) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeInt(0);

        DatagramPacket resolutionPacket = new DatagramPacket(
                byteArrayOutputStream.toByteArray(),
                byteArrayOutputStream.toByteArray().length,
                new InetSocketAddress(InetAddress.getByName(serverIP), serverPort));

        socket.send(resolutionPacket);
    }

    @Override
    public void sendScreenResolution(int screenWidth, int screenHeight) throws IOException {
        byte[] resolution = ByteBuffer.allocate(8).putInt(screenWidth).putInt(screenHeight).array();

        int attempt = 0;

        while (true) {
            DatagramPacket resolutionPacket = new DatagramPacket(
                    resolution,
                    resolution.length,
                    socketAddress);

            socket.send(resolutionPacket);

            try {
                socket.receive(resolutionPacket);
                if (resolutionPacket.getData().length == resolution.length)
                    break;
            } catch (SocketTimeoutException e) {
                attempt ++;
                if(attempt >= 3)
                    throw new IOException("can't connect");
            }
        }
    }

    @Override
    public Observable<Bitmap> getServerOutput() {
        Observable.OnSubscribe<Bitmap> onSubscribe = subscriber -> {
            try {
                while(true) {
                    socket.receive(inputPacket);
                    byte[] img;

                    ByteBuffer wrapped = ByteBuffer.wrap(inputPacket.getData());
                    int length = wrapped.getInt();

                    img = new byte[length];
                    wrapped.get(img, 0, length);

                    subscriber.onNext(BitmapFactory.decodeByteArray(img, 0, img.length));
                }
            } catch (Exception e) {
                if(e instanceof SocketTimeoutException)
                    disconnect();

                LoggerBus.getInstance().post(new LoggerBus.Log("Exception: " + e.getClass(), LOG_TAG, LoggerBus.Log.ERROR));
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
            }
        };

        return Observable.create(onSubscribe);
    }

    @Override
    public Action1<GameInput> getServerInput() {

        byte[] data = new byte[1+GameInput.BUFFER_SIZE];

        return gameInput -> {
            try {

                data[0] = gameInput.getType();
                for(int i=0; i<GameInput.BUFFER_SIZE; i++)
                    data[i+1] = gameInput.getPayload().get(i);

                outputPacket.setData(data);

                socket.send(outputPacket);
            } catch (Exception e) {
                throw new RuntimeException("Can't send game input", e);
            }
        };
    }

    @Override
    public void disconnect() {
        socket.disconnect();
        socket.close();

        LoggerBus.getInstance().post(new LoggerBus.Log("Ended connection with server.", LOG_TAG));
        EventBus.getInstance().post(new Events.ServerDisconnected());
    }
}
