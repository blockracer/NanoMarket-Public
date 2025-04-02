let socket = null;

self.addEventListener('message', function(event) {
    const { type, paymentID, paymentAddress } = event.data;

    if (type === 'CONNECT' || type === 'RECONNECT') {
        if (socket && socket.readyState === WebSocket.OPEN) {
            console.log('WebSocket already open, sending paymentID:', paymentID);
            socket.send(paymentID);


        }
        else {
            //socket = new WebSocket('ws://192.168.1.21:8888/checkpurchase');
            socket = new WebSocket('wss://checkpurchase.nanoriver.io');
        }

        // Create a new WebSocket connection

        socket.addEventListener('open', function() {
            console.log('WebSocket connection established.');
            self.postMessage({ type: 'STATUS', status: 'open' });

            if (paymentID) {
                socket.send(paymentID);
                console.log('Message sent:', paymentID);
                /*
               const interval = setInterval(() => {
                    if (socket.readyState === WebSocket.OPEN) {
                        socket.send(paymentID);
                    }
                    else {
                        console.log("WebSocket is not open. Stopping interval.");
                        clearInterval(interval);
                    }
                }, 5000); // Send every 5 seconds
                */
            }
        });

        socket.addEventListener('message', function(event) {
            console.log('Message from server:', event.data);
            self.postMessage({ type: 'STORE_MESSAGE', data: event.data });

            if (event.data.length === 36 || event.data.length === 4) {
                self.postMessage({ type: 'REDIRECT', paymentID, paymentAddress });
            }
        });

        socket.addEventListener('error', function(error) {
            console.error('WebSocket error:', error);
            self.postMessage({ type: 'STATUS', status: 'error', message: error.message });

            self.postMessage({ type: 'RECONNECT', paymentID, paymentAddress });
             setTimeout(() => {
                self.postMessage({ type: 'RECONNECT', paymentID, paymentAddress });
            }, 5000);

        });

        socket.addEventListener('close', function() {
            console.log('WebSocket closed.');
            self.postMessage({ type: 'STATUS', status: 'closed' });

            setTimeout(() => {
                self.postMessage({ type: 'RECONNECT', paymentID, paymentAddress });
            }, 5000);
        });
    }
});

