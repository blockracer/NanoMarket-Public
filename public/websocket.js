document.addEventListener('DOMContentLoaded', function() {
    // Helper function to add messages to the debug log
    function logMessage(message) {
        const debugLog = document.getElementById('debug-log');
        const logEntry = document.createElement('p');
        logEntry.textContent = message;
        debugLog.appendChild(logEntry);
        debugLog.scrollTop = debugLog.scrollHeight; // Scroll to the bottom
    }

    // Retrieve values from localStorage
    const responseId = localStorage.getItem('responseId');
  //  let responseId = "SHIT";
    const sellCost = parseFloat(localStorage.getItem('sellCost')).toFixed(5);
    const sellAddress = localStorage.getItem('sellAddress');

    logMessage('Response ID: ' + responseId);
    logMessage('Sell Cost: ' + sellCost);
    logMessage('Sell Address: ' + sellAddress);


    var resultBigDecimal = new Big(sellCost);
    // Move the decimal point right 30 times
    resultBigDecimal = resultBigDecimal.times('1e30');
    let formattedResult = new Big(resultBigDecimal).toFixed();

    // Get elements for QR code
    const qrContainer = document.getElementById('qr-container');
    const qrcodeDiv = document.getElementById('qrcode');

    if (sellAddress) {
        const qrLink = document.createElement('a');
        qrLink.href = `nano:${sellAddress}?amount=${formattedResult}`;
        const nanoUri = `nano:${sellAddress}?amount=${formattedResult}`;
        qrLink.target = '_blank'; // Opens the link in a new tab or app

        // Clear any existing QR code and add the link
        qrcodeDiv.innerHTML = '';
        qrcodeDiv.appendChild(qrLink);

        // Generate and display QR code
        try {
            new QRCode(qrLink, {
                text: nanoUri,
                width: 128,
                height: 128
            });
            logMessage('QR code generated successfully.');
        } catch (error) {
            logMessage('Error generating QR code: ' + error.message);
        }
                // Show QR code container
        qrContainer.style.display = 'flex';
    }

    // Display payment instructions
    const messagesDiv = document.getElementById('messages');
    const instructionsParagraph = document.createElement('p');
    if (sellCost && sellAddress) {
        //instructionsParagraph.innerHTML = `Please pay the collateral of Ӿ${sellCost} to the address' + "<br>" + ${sellAddress}.`;
       // instructionsParagraph.innerHTML = `Please pay the collateral of Ӿ${sellCost} to the address:<br>${sellAddress}.`;
        instructionsParagraph.innerHTML = `Please pay the collateral of Ӿ${sellCost} to the address:<br>
        <span id="copyAddress" style="cursor: pointer; color: blue; text-decoration: underline;">${sellAddress}</span>`;
    } else {
        instructionsParagraph.textContent = 'Payment details are not available.';
    }
    messagesDiv.appendChild(instructionsParagraph);
    // Delay the event listener to ensure the element exists
    //
    // Add click-to-copy functionality with execCommand fallback
    setTimeout(() => {
        const copyElement = document.getElementById('copyAddress');
        if (copyElement) {
            copyElement.addEventListener('click', function () {
                const tempInput = document.createElement("input");
                tempInput.value = sellAddress;
                document.body.appendChild(tempInput);
                tempInput.select();
                document.execCommand("copy");  // Fallback method
                document.body.removeChild(tempInput);

                alert("Address copied to clipboard!");
            });
        } else {
            console.error("Element 'copyAddress' not found.");
        }
    }, 500);
    
    
    
    // Initialize Web Worker
    let worker = new Worker('websocket-worker.js');

    // Send initial connection request to worker
        setInterval(() => {
            worker.postMessage({ type: 'CONNECT', responseId, sellAddress });
        }, 5000); // Send every 5 seconds

    // Handle messages from the worker
    worker.addEventListener('message', function(event) {
        const { type, cancelID, status, message } = event.data;
            logMessage('Received data: ' + JSON.stringify(event.data));
        if (type === 'REDIRECT') {
            logMessage('Received REDIRECT message from worker. Redirecting...');
            window.location.href = `listingform.html?id=${encodeURIComponent(responseId)}&cancelid=${cancelID}`;
        } else if (type === 'STATUS') {
            if (status === 'open') {
                logMessage('WebSocket connection is open.');
            } else if (status === 'closed') {
                logMessage('WebSocket connection is closed.');
            } else if (status === 'error') {
                logMessage('WebSocket error: ' + message);
                logMessage('WebSocket error message: ' + (message || 'No message provided'));
            }
        }
        else if (type === 'STORE_MESSAGE') {
        // Store WebSocket messages in an array
        //worker.postMessage({ type: 'CONNECT', responseId, sellAddress });
        //storedMessages.push(data);
        logMessage('Stored WebSocket message: ' + event.data);
    }
    });

    worker.postMessage({ type: 'CONNECT', responseId, sellAddress });

    // Handle errors from the worker
    worker.addEventListener('error', function(error) {
        logMessage('Web Worker error: ' + error.message);
    });

    // Handle visibility change
    function handleVisibilityChange() {
        if (document.visibilityState === 'visible') {
            logMessage('Page is visibleee. Reconnecting WebSocket...');
            logMessage('Response Id: ' + responseId);
            logMessage('sell address: ' + sellAddress);
            worker.postMessage({ type: 'CONNECT', responseId, sellAddress });
        }
    }


});


