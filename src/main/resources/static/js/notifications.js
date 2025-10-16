let stompClient = null;
let notificationCount = 0;

function connectNotifications() {
    console.log("🔌 Connecting to WebSocket...");

    const socket = new SockJS('/ws');   // ✅ must match backend endpoint
    stompClient = Stomp.over(socket);

    // silence default STOMP logs (optional)
    stompClient.debug = null;

    stompClient.connect({}, function (frame) {
        console.log("✅ Connected to WebSocket:", frame);

        // ✅ subscribe to user queue
        console.log("📡 Subscribing to /user/queue/notifications ...");

        stompClient.subscribe('/user/queue/notifications', (notification) => {
            console.log("📩 Got notification frame:", notification);
            console.log("📩 Raw body:", notification.body);

            try {
                const data = JSON.parse(notification.body);
                console.log("✅ Parsed JSON:", data);

                if (data.message) {
                    console.log("🔔 Adding notification to UI:", data.message);
                    addNotification(data.message);
                } else {
                    console.warn("⚠️ Notification payload missing 'message' field:", data);
                }
            } catch (err) {
                console.error("❌ Parse error:", err, "Body was:", notification.body);
            }
        }, (error) => {
            console.error("❌ Subscription error:", error);
        });

    }, function (error) {
        console.error("❌ STOMP connection error:", error);
    });
}

function addNotification(message) {
    console.log("📝 addNotification() called with message:", message);

    const list = document.getElementById('notifications-list');
    const badge = document.getElementById('notification-count');

    if (!list || !badge) {
        console.error("❌ Missing #notifications-list or #notification-count element in DOM!");
        return;
    }

    // remove "No notifications yet"
    if (list.children.length === 1 &&
        list.children[0].tagName === "LI" &&
        list.children[0].innerText.includes("No notifications")) {
        console.log("🗑️ Clearing placeholder 'No notifications yet'");
        list.innerHTML = "";
    }

    // create new notification entry
    const li = document.createElement("li");
    li.textContent = message;

    // insert new notification on top
    list.prepend(li);

    // update count
    notificationCount++;
    badge.textContent = notificationCount;
    badge.style.display = "inline-block";

    console.log("✅ Notification added. Count now:", notificationCount);
}

// Optional: reset notifications
function clearNotifications() {
    console.log("🧹 Clearing notifications");
    const list = document.getElementById('notifications-list');
    const badge = document.getElementById('notification-count');

    list.innerHTML = "<li><em>No notifications yet</em></li>";
    notificationCount = 0;
    badge.style.display = "none";
}

window.addEventListener("load", connectNotifications);
