package xyz.qianbidao;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.steam.discovery.ServerRecord;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author lngtr
 * @since 2018-02-23
 */
public class SampleLogonUtils {

    private SteamClient steamClient;

    private CallbackManager manager;

    private SteamUser steamUser;

    private volatile boolean isRunning;

    private String user;

    private String pass;

    private byte[] bf;

    private LogonMsgCallBack callBackFunction;

    private String proxyHost;
    private Integer port;
    private ServerRecord serverRecord;

    public void setServerRecord(ServerRecord serverRecord) {
        this.serverRecord = serverRecord;
    }

    public SampleLogonUtils(String user, String pass, File keyFile) {
        this.user = user;
        this.pass = pass;
        if (null != keyFile) {
            try {
                bf = calculateSHA1(keyFile);
            } catch (Exception e) {}
        }
    }

    public SampleLogonUtils(String user, String pass, File keyFile,String proxyHost, Integer port) {
        this(user,pass,keyFile);
        this.proxyHost = proxyHost;
        this.port = port;
    }


    private byte[] calculateSHA1(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        InputStream fis = new FileInputStream(file);
        int n = 0;
        byte[] buffer = new byte[8192];
        while (n != -1) {
            n = fis.read(buffer);
            if (n > 0) {
                digest.update(buffer, 0, n);
            }
        }
        return digest.digest();
    }
    public void access(LogonMsgCallBack callBack){
        this.callBackFunction = callBack;

        // create our steamclient instance
        if(proxyHost==null||port==null){
            steamClient = new SteamClient();
        }else {
            steamClient = new SteamClient(proxyHost,port);
        }
        // create the callback manager which will route callbacks to function calls
        manager = new CallbackManager(steamClient);
        // get the steamuser handler, which is used for logging on after successfully connecting
        steamUser = steamClient.getHandler(SteamUser.class);

        // register a few callbacks we're interested in
        // these are registered upon creation to a callback manager, which will then route the callbacks
        // to the functions specified
        manager.subscribe(ConnectedCallback.class, this::onConnected);
        manager.subscribe(DisconnectedCallback.class, this::onDisconnected);

        manager.subscribe(LoggedOnCallback.class, this::onLoggedOn);
        manager.subscribe(LoggedOffCallback.class, this::onLoggedOff);

        isRunning = true;
        steamClient.connect(serverRecord);
        // create our callback handling loop
        while (isRunning) {
            // in order for the callbacks to get routed, they need to be handled by the manager
            manager.runWaitCallbacks(1000L);
        }
    }

    private void onConnected(ConnectedCallback callback) {
        LogOnDetails details = new LogOnDetails();
        details.setUsername(user);
        details.setPassword(pass);
        if (null != bf) {
            details.setSentryFileHash(bf);
        }

        steamUser.logOn(details);
    }

    private void onDisconnected(DisconnectedCallback callback) {
        isRunning = false;
        callBackFunction.callback(new LoggedOnCallback(EResult.RemoteDisconnect));
    }

    private void onLoggedOn(LoggedOnCallback callback) {
        isRunning = false;
        callBackFunction.callback(callback);
        steamUser.logOff();
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        isRunning = false;
    }
}