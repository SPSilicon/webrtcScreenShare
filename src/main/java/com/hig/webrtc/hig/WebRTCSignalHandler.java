package com.hig.webrtc.hig;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WebRTCSignalHandler extends TextWebSocketHandler{
    Map<String,WebSocketSession> sessions = new ConcurrentHashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // TODO Auto-generated method stub
        String roomID = UUID.randomUUID().toString();
        SendDto sd = new SendDto("roomID", roomID);
        sessions.put(roomID,session);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(sd)));
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // TODO Auto-generated method stub
        
        RecvDto signal = objectMapper.readValue(message.toString(),RecvDto.class);
        switch(signal.getName()) {

            case "offer":
                if(sessions.getOrDefault(signal.getTo(),null)==null) {
                    sessions.put(signal.getTo(), session);
                    session.sendMessage(new TextMessage("room opened"));
                } else {
                    WebSocketSession opSession = sessions.get(signal.getTo());
                    if(opSession.isOpen()) {
                        opSession.sendMessage(new TextMessage(signal.getData()));
                    }
                }
                break;

            case "answer":
                //AnswerDto answer = objectMapper.readValue(signal.getData(),AnswerDto.class);
                WebSocketSession opSession = sessions.get(signal.getTo());
                if(opSession.isOpen()) {
                    opSession.sendMessage(new TextMessage(signal.getData()));
                }
                break;

            case "candidate":
                WebSocketSession opSession = sessions.get(signal.getTo());
                if(opSession.isOpen()) {
                    opSession.sendMessage(new TextMessage(signal.getData()));
                }
                break;
                
            case "close":
                SendDto sd = new SendDto("close", "");
                for(WebSocketSession s : sessions.get(signal.getTo()) ) {
                    if(s.isOpen()) {
                        s.sendMessage(new TextMessage(objectMapper.writeValueAsString(sd)));
                    }
                }
                sessions.remove(signal.getTo());
            break;
            //TODO accpet json parsing 후 전송 구현
        }
        super.handleTextMessage(session, message);
    }
}
