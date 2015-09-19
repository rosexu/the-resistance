$(document).ready(function() {
    console.log("I'm ready");
    webSocketTest();
});

function webSocketTest()
{
    if ("WebSocket" in window)
    {
        var ws = new WebSocket("ws://localhost:9000/ws");

        ws.onopen = function() {
            // Web Socket is connected, send data using send()
            ws.send("Message to send");
            alert("Message is sent...");
        };

        ws.onmessage = function (evt) {
            var received_msg = evt.data;
            alert("Message is received...");
        };

        ws.onclose = function() {
            alert("Connection is closed...");
        };
    }

    else
    {
        alert("WebSocket NOT supported by your Browser!");
    }
}