//import imageCompression from '/node_modules/browser-image-compression/dist/browser-image-compression.js';

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('dataForm');
    const resultDiv = document.getElementById('result');

    // Get the ID from the URL
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get('id');
    console.log(id);
    const cancelID = urlParams.get('cancelid');

    form.addEventListener('submit', async function(event) {
        event.preventDefault();

        // Validate description length
        if (descriptionInput.value.length > maxChars) {
            //alert('Description exceeds the maximum allowed characters.');
            document.getElementById("result").textContent = "Description exceeds the maximum allowed characters";
            return; // Stop form submission
        }

        // Retrieve form data
        const description = encodeToBase64(document.getElementById('description').value);
        const email = document.getElementById('email').value;
        const title = document.getElementById('title').value;
        const tag1 = document.getElementById('tag1').value;
        const tag2 = document.getElementById('tag2').value;
        const tag3 = document.getElementById('tag3').value;
        const imageFile = document.getElementById('image').files[0];

        if (!imageFile) {
            console.error('No image file selected');
            return;
        }

        try {
            let imageBase64;

            // Check if image file size is over 500 KB
            if (imageFile.size > 500 * 1024) {
                // Compress the image using browser-image-compression
                const options = {
                    maxSizeMB: 0.5, // Maximum size in MB (500 KB)
                    maxWidthOrHeight: 1024, // Maximum width or height
                    useWebWorker: true, // Use a web worker for better performance
                };

                const compressedFile = await imageCompression(imageFile, options);

                // Convert the compressed file to Base64
                imageBase64 = await fileToBase64(compressedFile);
            } else {
                // If image size is not over 500 KB, proceed without compression
                imageBase64 = await fileToBase64(imageFile);
            }

            // Create JSON object with the image
            const data = {
                description: description,
                email: email,
                title: title,
                tag1: tag1,
                tag2: tag2,
                tag3: tag3,
                image: imageBase64
            };

            // Send POST request
            const response = await fetch(`/submitsell/${id}/${cancelID}/false` , {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            const result = await response.json();
            resultDiv.textContent = JSON.stringify(result, null, 2);

            if (result.isValid) {
                window.location.href = `/listing/${id}`;
            }
        } catch (error) {
            console.error('Error:', error);
            resultDiv.textContent = 'An error occurred. Please try again.';
        }
    });

    // Helper function to convert a file to Base64
    function fileToBase64(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onloadend = () => {
                const base64String = reader.result.split(',')[1];
                resolve(base64String);
            };
            reader.onerror = reject;
            reader.readAsDataURL(file);
        });
    }

    // Helper function to encode text to Base64
    /*
    function encodeToBase64(text) {
        return btoa(encodeURIComponent(text));
    }
    function encodeToBase64(text) {
    // Create a buffer from the text string
        const encodedString = btoa(text);
        return encodedString;
    }

    */
    function encodeToBase64(text) {
        return btoa(unescape(encodeURIComponent(text)));
    }
});

  const descriptionInput = document.getElementById("description");
    const charCount = document.getElementById("charCount");
    const maxChars = 10000;

    descriptionInput.addEventListener("input", function () {
        const remaining = maxChars - descriptionInput.value.length;
        charCount.textContent = remaining + " characters remaining";

        if (remaining < 0) {
            charCount.style.color = "red";
        } else {
            charCount.style.color = "black";
        }
    });
