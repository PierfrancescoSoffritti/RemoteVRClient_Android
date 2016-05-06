package com.pierfrancescosoffritti.remotevrclient.io.connections;

import android.graphics.Bitmap;

import com.pierfrancescosoffritti.remotevrclient.io.data.GameInput;

import java.io.IOException;

import rx.Observable;
import rx.functions.Action1;

/**
 * This interface represents the contract with the server.
 * <br/>
 * Should be implemented by each class that wants to communicate with the server.
 *
 * @author Pierfrancesco Soffritti
 */
public interface ServerIO {

    /**
     * Sends the resolution of the screen (in pixel) to the server.
     * @param screenWidth device screenWidth in pixels.
     * @param screenHeight device screenHeight in pixels.
     */
    void sendScreenResolution(int screenWidth, int screenHeight) throws IOException;

    /**
     * The output of the server is a series of images.
     * @return an Observable representing the server output, which is a stream of images.
     */
    Observable<Bitmap> getServerOutput();

    /**
     * The inputs of the server are the game commands.
     * @return an Action1 that takes a {@link GameInput} and sends its type and payload to the server.
     */
    Action1<GameInput> getServerInput();

    void disconnect();
}
