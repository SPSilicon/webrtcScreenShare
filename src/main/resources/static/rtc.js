'use strict'


const startButton = document.getElementById('startButton');
const callButton = document.getElementById('callButton');
const stopButton = document.getElementById('stopButton');
const roomID = document.getElementById('roomId');
const callTo = document.getElementById("callTo");
const me = document.querySelector('video#me');
const opp = document.querySelector('video#opp');
const conn = new WebSocket('ws://spsi.kro.kr:8080/socket');
var peerConnection;
var dataChannel;
var videoStream;
var audioStream;


function send(message) {
    conn.send(JSON.stringify(message));
}

conn.onmessage = function(message) {
    
    switch(message.event) {
        case "offer":
            var data = JSON.parse(message.data);
            handleOffer(data);
            break;
        case "candidate":
            var data = JSON.parse(message.data);
            handleCandidate(data);
            break;
        case "answer":
            var data = JSON.parse(message.data);
            handleAnswer(data);
            break;
        case "roomID":
            roomID.value = message.data;
            break;
        case "close":
            handleClose();
            break;
    }
};
const offerOptions = {
    offerToReceiveAudio: 1,
    offerToReceiveVideo: 1
}

function gotStream(stream) {
    console.log("stream received");
}

function start() {
    startButton.disabled = true;
    callButton.disabled = false;
    stopButton.disabled = false;
    console.log('Requesting local stream');
    startButton.disabled = true;
    const mediaStream = navigator.mediaDevices
        .getUserMedia({
          audio: true,
          video: true
        })
        .then(gotStream)
        .catch(e => console.log('getUserMedia() error: ', e));
    var configuration = null;
    peerConnection = new RTCPeerConnection(configuration);
    peerConnection.onicecandidate = (candidate) => {
        if(candidate.candidate){
            send({
                event : "candidate",
                to : callTo.value,
                data : candidate.candidate
            });
        }
    }
    dataChannel = peerConnection.createDataChannel("dataChannel",{reliable:true});
    dataChannel.onerror = function(e) {console.log("error: ",e)};
    dataChannel.onclose = function(e) {console.log("error: ",e)};

}

function call() {
    peerConnection.createOffer(function(offer){
        send({
            event : "offer",
            to : callTo.value,
            data : offer
        });
        peerConnection.setLocalDescription(offer);
    }, function(error) {
        console.log("error : ", error);
    });

}

callButton.onclick = call;

function handleOffer(data) {
    peerConnection.setRemoteDescription(data);
    peerConnection.createAnswer(function(answer) {
        peerConnection.setLocalDescription(answer);
        send({
            event : "answer",
            to : callTo.value,
            data : answer
        });
    },function(error) {

    });
}

function handleAnswer(data) {
    peerConnection.setRemoteDescription(new RTCSessionDescription(answer));
}

function handleCandidate(candidate) {
    peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
}

function handleClose() {
    startButton.disabled = false;
    stopButton.disabled = true;
    peerConnection.close();
}