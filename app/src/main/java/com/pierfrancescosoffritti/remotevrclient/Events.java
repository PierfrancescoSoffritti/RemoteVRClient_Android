package com.pierfrancescosoffritti.remotevrclient;

/**
 * Created by  Pierfrancesco on 06/03/2016.
 */
public class Events {
    public static class ServerConnecting {
    }

    public static class ServerConnected {
    }

    public static class ServerDisconnected {
    }

    public static class DisconnectServer {
    }

    public static class RemoteView_SwipeTopBottom {
    }

    public static class GoFullScreen {
        private boolean goFullScreen;

        public GoFullScreen(boolean goFullScreen) {
            this.goFullScreen = goFullScreen;
        }

        public boolean isGoFullScreen() {
            return goFullScreen;
        }
    }
}
