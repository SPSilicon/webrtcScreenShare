'use strict'


const startButton = document.getElementById('startButton');
const callButton = document.getElementById('callButton');
const stopButton = document.getElementById('stopButton');
const roomID = document.getElementById('roomId');
const callTo = document.getElementById("callTo");
const me = document.querySelector('video#me');
const opp = document.querySelector('video#opp');
const conn = new WebSocket('wss://spsi.kro.kr/signal');
var peerConnection;
var dataChannel;
var videoStream;
var audioStream;


function send(message) {
    conn.send(JSON.stringify(message));
}

conn.onclose = function(e) {
    console.log("conn closed : ",e);
};

conn.onmessage = function(message) {
    var msg = JSON.parse(message.data);
    var data = msg.data;
    switch(msg.event) {
        case "offer":
            callTo.value = msg.sender
            handleOffer(data);
            break;
        case "candidate":
            handleCandidate(data);
            break;
        case "answer":
            handleAnswer(data);
            break;
        case "roomID":
            roomID.innerText = data.roomID;
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

async function gotStream(stream) {
    console.log("stream received");
    me.srcObject = stream;
    peerConnection.addStream(stream);
}

async function start() {
    startButton.disabled = true;
    callButton.disabled = false;
    stopButton.disabled = false;
    console.log('Requesting local stream');
    startButton.disabled = true;

    var configuration = {
        "iceServers" : [ {
            "url" : "stun:stun2.1.google.com:19302"
        } ]
    };
    peerConnection = new RTCPeerConnection(configuration);
    peerConnection.onicecandidate = (candidate) => {
        if(candidate.candidate){
            send({
                event : "candidate",
                from : roomID.innerText,
                to : callTo.value,
                data : candidate.candidate
            });
        }
    }
    peerConnection.ondatachannel = function (event) {
        dataChannel = event.channel;
    };

    const mediaStream = navigator.mediaDevices
    .getUserMedia({
      audio: true,
      video: true
    })
    .then(gotStream)
    .catch(e => console.log('getUserMedia() error: ', e));


    peerConnection.onaddstream = (event)=>{
        opp.srcObject = event.stream;
    };
    dataChannel = peerConnection.createDataChannel("dataChannel",{reliable:true});
    dataChannel.onmessage = function(event) {
        console.log("Message:", event.data);
    };
    dataChannel.onerror = function(e) {console.log("error: ",e)};
    dataChannel.onclose = function(e) {console.log("error: ",e)};
}
startButton.onclick = start;


function call() {
    peerConnection.createOffer(function(offer){
        send({
            event : "offer",
            from : roomID.innerText,
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
    console.log("offer received",data);
    peerConnection.setRemoteDescription(data);
    peerConnection.createAnswer(function(answer) {
        peerConnection.setLocalDescription(answer);
        send({
            event : "answer",
            from : roomID.innerText,
            to : callTo.value,
            data : answer
        });
    },function(error) {
        console.log("error : ",error);
    });
}

function handleAnswer(answer) {
    console.log("answer received : ",answer);
    peerConnection.setRemoteDescription(new RTCSessionDescription(answer));
}

function handleCandidate(candidate) {
    console.log("candidate received",candidate);
    peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
}

function handleClose() {
    console.log('Ending call');
    peerConnection.close();
    peerConnection = null;
    stopButton.disabled = true;
    startButton.disabled =false;
    callButton.disabled = false;
}

stopButton.onclick = () =>{
    send({
        event : "close",
        from : roomID.innerText,
        to : callTo.value,
        data : ""
    });
    handleClose();
};