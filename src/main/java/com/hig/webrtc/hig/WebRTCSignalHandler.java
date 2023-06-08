package com.hig.webrtc.hig;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class WebRTCSignalHandler extends TextWebSocketHandler{
    Map<String,WebSocketSession> sessions = new ConcurrentHashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // TODO Auto-generated method stub
        String roomID = UUID.randomUUID().toString();
        SendDto sd = new SendDto("roomID","", objectMapper.createObjectNode().put("roomID",roomID));
        sessions.put(roomID,session);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(sd)));
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // TODO Auto-generated method stub
        super.afterConnectionClosed(session, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // TODO Auto-generated method stub
        
        RecvDto signal = objectMapper.readValue(message.getPayload().toString(),RecvDto.class);
        String to = signal.getTo();
        String from = signal.getFrom();
        JsonNode data = signal.getData();
        WebSocketSession opSession = sessions.get(to);    
        SendDto sd;
        switch(signal.getEvent()) {

            case "offer":
                if(opSession!=null) {
                    if(opSession.isOpen()) {
                        sd = new SendDto("offer",from, data);
                        opSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(sd)));
                    } else {
                        sd = new SendDto("error",to, objectMapper.createObjectNode().put("error","invalid room"));
                        sessions.remove(to);
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(sd)));
                    }        
                }
                break;

            case "answer":
                //AnswerDto answer = objectMapper.readValue(signal.getData(),AnswerDto.class);
                if(opSession!=null&&opSession.isOpen()) {
                    sd = new SendDto("answer",from, data);
                    opSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(sd)));
                }
                break;

            case "candidate":
                //WebSocketSession opSession = sessions.get(signal.getTo());
                if(opSession!=null&&opSession.isOpen()) {
                    sd = new SendDto("candidate",from, data);
                    opSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(sd)));
                }
                break;
                
            case "close":
                if(opSession!=null&&opSession.isOpen()) {
                    sd = new SendDto("close",from, null);
                    opSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(sd)));
                }
            break;
            //TODO accpet json parsing 후 전송 구현
        }
        super.handleTextMessage(session, message);
    }
}
