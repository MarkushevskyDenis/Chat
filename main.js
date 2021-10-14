var request = new XMLHttpRequest();
var request2 = new XMLHttpRequest();

getMessage();

function getMessage() {
  request.open("GET", "http://localhost:8080");
  request.onload = function() {
     addMessage(request.response);
     getMessage();
  };
  request.send();
}

function sendMessage() {
  var message;

  message = document.getElementById("message").value

  request2.open("POST", "http://localhost:8080");

  request2.onload = function() {
     addMessage(message);
  };
  request2.send(message);
}

function addMessage(text) {
  var li = document.createElement("li");
  li.innerHTML = text;
  ul = document.getElementById("chat");
  ul.appendChild(li);
}
