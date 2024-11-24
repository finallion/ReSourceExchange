document.addEventListener('DOMContentLoaded', () => {
    const chatId = document.getElementById('chatId').value;
    const loggedInUser = document.getElementById('loggedInUser').value;

    const client = new StompJs.Client({
        brokerURL: 'ws://localhost:8080/ws',
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
    });

    client.onConnect = (frame) => {
        console.log("Connected: " + frame);

        client.subscribe(`/topic/chat/${chatId}`, (message) => {
            const msgContent = JSON.parse(message.body);
            showReceivedMessage(msgContent);
        });
    };

    client.activate();

    function sendMessage() {
        const content = document.getElementById('message-input').value;

        if (content.trim() === "") {
            alert("Message cannot be empty!");
            return;
        }

        const message = {
            sender: loggedInUser,
            content: content
        };

        client.publish({
            destination: `/app/chat/${chatId}`,
            body: JSON.stringify(message)
        });

        document.getElementById('message-input').value = '';
    }

    function showReceivedMessage(message) {
        const messagesDiv = document.getElementById('messages');
        const messageElement = document.createElement('p');
        messageElement.textContent = `${message.sender}: ${message.content}`;
        messagesDiv.appendChild(messageElement);
    }

    document.querySelector('button').addEventListener('click', sendMessage);
});
