document.addEventListener('DOMContentLoaded', () => {
    const chatId = document.getElementById('chatId').value;
    const loggedInUser = document.getElementById('loggedInUser').value;

    const client = new StompJs.Client({
        webSocketFactory: () => new SockJS('/ws'),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: (frame) => {
            console.log("Connected: " + frame);
            console.log("Chat-ID: " + chatId);
            client.subscribe(`/topic/chat/${chatId}`, (message) => {
                const msgContent = JSON.parse(message.body);

                showReceivedMessage(msgContent);
            });
        }
    });
    client.activate();

    function sendMessage() {
        const content = document.getElementById('message-input').value;

        console.log("Sending message: " + content);
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
            body: JSON.stringify(message),
            headers: {
                'content-type': 'application/json'
            }
        });

        document.getElementById('message-input').value = '';
    }

    function showReceivedMessage(message) {
        console.log("Receiving: " + message);
        const messagesDiv = document.getElementById('messages');
        const messageElement = document.createElement('p');
        messageElement.textContent = `${message.sender}: ${message.content}`;
        messagesDiv.appendChild(messageElement);
    }

    document.getElementById('send-button').addEventListener('click', sendMessage);
});
