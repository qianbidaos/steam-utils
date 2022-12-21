package xyz.qianbidao;


import in.dragonbra.javasteam.enums.EResult;

import java.io.IOException;
import java.net.*;

/**
 * @auther 铅笔刀
 * @date 2022/10/17
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        Authenticator.setDefault(new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return (new PasswordAuthentication("13925", "13925".toCharArray()));
            }
        });

        SampleLogonUtils qianbidao = new SampleLogonUtils("sandman9r", "javits",null,null,null);
        qianbidao.access(loglonMsgCallBack->{
            if(loglonMsgCallBack.getResult() == EResult.RemoteDisconnect){
                System.out.printf("disconnect");
            }else {
                System.out.println("loglonMsgCallBack.getResult() " + loglonMsgCallBack.getResult());
            }
        });
    }

}
