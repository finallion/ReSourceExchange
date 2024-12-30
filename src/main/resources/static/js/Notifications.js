    document.addEventListener("DOMContentLoaded", function () {
    const notificationDropdown = document.getElementById("notificationDropdown");
    const notificationList = document.getElementById("notificationMenu");
    const notificationCount = document.getElementById("notificationCount");
    const noNotifications = document.getElementById("noNotifications");
    const notificationTemplate = document.getElementById("notificationTemplate");

    function loadNotifications() {
    fetch('/notifications', {
    method: 'GET',
    headers: {
    'Content-Type': 'application/json'
}
})
    .then(response => response.json())
    .then(data => {
    notificationCount.textContent = data.count > 0 ? data.count : '';
    notificationCount.style.display = data.count > 0 ? 'inline-block' : 'none';

    if (data.notifications.length === 0) {
    noNotifications.classList.remove('d-none');
    notificationList.innerHTML = '';
} else {
    noNotifications.classList.add('d-none');
    notificationList.innerHTML = '';
    data.notifications.forEach(notification => {
    const notificationItem = notificationTemplate.cloneNode(true);
    notificationItem.classList.remove('d-none');
    notificationItem.querySelector('.notification-message').textContent = notification.message;
    const deleteIcon = notificationItem.querySelector('.delete-icon');
    const detailsIcon = notificationItem.querySelector('.details-icon');

    deleteIcon.setAttribute('data-id', notification.id);
    detailsIcon.setAttribute('href', notification.link);

    deleteIcon.addEventListener('click', function (event) {
    event.preventDefault();
    const id = this.getAttribute('data-id');
    deleteNotification(id);
});

    notificationList.appendChild(notificationItem);
});
}
})
    .catch(error => console.error('Fehler beim Laden der Benachrichtigungen:', error));
}

    notificationDropdown.addEventListener("click", function () {
    loadNotifications();
});

    function deleteNotification(id) {
    console.log(`Versuche, Benachrichtigung mit ID ${id} zu löschen...`);

    fetch(`/notifications/delete/${id}`, {
    method: 'GET',
    headers: {
    'Content-Type': 'application/json'
}
})
    .then(response => {
    console.log(`Antwort vom Server: ${response.status} - ${response.statusText}`);

    if (response.ok) {
    console.log(`Benachrichtigung mit ID ${id} wurde erfolgreich gelöscht.`);
    loadNotifications();
} else {
    console.error(`Fehler beim Löschen der Benachrichtigung mit ID ${id}. Status: ${response.status}`);
}
})
    .catch(error => {
    console.error('Fehler beim Löschen der Benachrichtigung:', error);
});
}

    loadNotifications();
});
