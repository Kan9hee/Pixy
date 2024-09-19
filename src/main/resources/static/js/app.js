var gumStream;
var rec;
var input;
const sessionID = {};

var audioContext = window.AudioContext || window.webkitAudioContext;

var recordButton = document.getElementById("recordButton");
var stopButton = document.getElementById("stopButton");

recordButton.addEventListener("click", startRecording);
document.getElementById("stopButton").addEventListener("click", function() {
    stopRecording();
    document.getElementById('recording-controls').style.display = 'none';
    document.getElementById('loading-section').style.display = 'flex';
});

function goToMain() {
    window.location.href = `/main`;
}

function startRecording() {
	console.log("recordButton clicked");

    var constraints = { audio: true, video:false }
	recordButton.disabled = true;
	recordButton.style.display = 'none';
	stopButton.disabled = false;
	stopButton.style.display = 'flex';
	document.getElementById("return-main").style.display = 'none';

	navigator.mediaDevices.getUserMedia(constraints).then(function(stream) {
		console.log("getUserMedia() success, stream created, initializing Recorder.js ...");

		audioContext = new AudioContext();
		gumStream = stream;
		input = audioContext.createMediaStreamSource(stream);

		rec = new Recorder(input,{numChannels:1})
		rec.record()

		console.log("Recording started");
	}).catch(function(err) {
	    console.error("getUserMedia() failed: ", err);
    	recordButton.disabled = false;
    	stopButton.disabled = true;
	});
}

function stopRecording() {
	console.log("stopButton clicked");
	stopButton.disabled = true;
	recordButton.disabled = false;
	rec.stop();
	gumStream.getAudioTracks()[0].stop();
	rec.exportWAV(sendRecordingToServer);
}

function sendRecordingToServer(blob) {
    if (sessionID.value) {
        var filename = new Date().toISOString();
        const formData = new FormData();
        formData.append('voice', blob, filename+".wav");
        formData.append('sessionId', sessionID.value);

        fetch('/Api/uploadWav', {
                method: 'POST',
                body: formData
            })
            .catch(error => {
                console.error('녹음 파일을 서버로 전송하는 중 오류가 발생했습니다:', error);
            });
    } else {
        console.error('Session ID가 없습니다.');
    }
}

function connectWebSocket(sessionId) {
    if (!sessionId) {
        console.error('sessionID가 없습니다.');
        return;
    }

    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        stompClient.subscribe('/answerCheck/' + sessionId + '/loading', function(message) {
            const progressData = JSON.parse(message.body);

            document.getElementById('status-text').innerText = progressData.message;

            const progressBar = document.getElementById('progress-bar');
            const progressPercentage = (progressData.currentStep / progressData.totalSteps) * 100;
            progressBar.style.width = progressPercentage + '%';

            if (progressData.currentStep === progressData.totalSteps) {
                console.log('Loading complete!');
                document.getElementById('loading-section').style.display = 'none';
                document.getElementById('result-section').style.display = 'flex';
                document.getElementById("return-main").style.display = 'flex';
                loadResults();
                stompClient.disconnect();
            }
        });
    });

    window.onbeforeunload = function() {
        if (stompClient && stompClient.connected) {
            stompClient.disconnect(function() {
                console.log('Websocket stomp disconnected');
            });
        }
    };
}

function loadResults() {
    if (sessionID.value) {
        fetch(`/Api/getUsageResult?sessionId=${sessionID.value}`)
            .then(response => response.json())
            .then(data => {
                document.getElementById('beforeText').innerText = data.beforeString || "초기 인식 문장을 불러올 수 없습니다.";
                document.getElementById('afterText').innerText = data.afterString || "피드백된 문장을 불러올 수 없습니다.";
            })
            .catch(error => console.error('Error fetching usage result:', error));
    } else {
        console.error('sessionId가 존재하지 않습니다');
    }
}

window.addEventListener('load', function() {
    fetch('/Api/generateSession')
    .then(response => {
        if (response.ok) {
            return response.text();
        } else {
            throw new Error('세션 ID를 가져오지 못했습니다.');
        }
    })
    .then(uuid => {
        sessionID.value = uuid;
        console.log('Session ID:', sessionID.value);
        connectWebSocket(sessionID.value);
    })
    .catch(error => {
        console.error('오류:', error);
    });

    const urlParams = new URLSearchParams(window.location.search);
    const difficulty = urlParams.get('difficulty');
    if (difficulty) {
        fetch(`/Api/getQuestion?difficulty=${difficulty}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('네트워크 응답이 없습니다.');
                }
                return response.text();
            })
            .then(data => {
                document.getElementById('question').textContent = `${data}`;
                document.getElementById('page-status').textContent = `모의테스트 - 난이도 ${difficulty}`
            })
            .catch(error => {
                console.error(error);
            });
    } else {
        fetch(`/Api/getQuestion?difficulty=0`)
            .then(response => {
                if (!response.ok) {
                throw new Error('네트워크 응답이 없습니다.');
                }
                return response.text();
            })
            .then(data => {
                document.getElementById('question').textContent = `${data}`;
                document.getElementById('page-status').textContent = "프리스타일"
            })
            .catch(error => {
                console.error(error);
            });
    }
});