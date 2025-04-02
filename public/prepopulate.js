document.addEventListener("DOMContentLoaded", async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get("id");

    if (!id) {
        console.error("No ID provided in the URL.");
        return;
    }

    try {
        const response = await fetch(`/modify/${id}`);
        if (!response.ok) {
            throw new Error("Failed to fetch data");
        }

        const data = await response.json();

        // Function to decode Base64
        function decodeBase64(str) {
            try {
                return decodeURIComponent(atob(str)); // Decode Base64 and URI component
            } catch (e) {
                console.error("Error decoding Base64:", e);
                return str; // Fallback to original value if decoding fails
            }
        }

        console.log("country: " + data.country);

        // Prepopulate form fields
        document.getElementById("title").value = data.title || "";
        document.getElementById("description").value = data.description ? decodeBase64(data.description) : "";
        document.getElementById("email").value = data.email || "";
        document.getElementById("tag1").value = data.tag1 || "";
        document.getElementById("tag2").value = data.tag2 || "";
        document.getElementById("tag3").value = data.tag3 || "";
        document.getElementById("countries").value = data.country || "";

    } catch (error) {
        console.error("Error fetching form data:", error);
    }

    // Function to encode text to Base64
    
    function encodeToBase64(text) {
        return btoa(unescape(encodeURIComponent(text)));
    }


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

    // Function to convert a file to Base64
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

    // Add event listener for form submission
    document.getElementById('dataForm').addEventListener('submit', async function(event) {
        event.preventDefault(); // Prevent default form submission


        
        const descriptionInput = document.getElementById("description");
        const maxChars = 10000;

        // Validate description length
        if (descriptionInput.value.length > maxChars) {
            document.getElementById("result").textContent = "Description exceeds the maximum allowed characters.";
            return; // Stop form submission
        }

        const urlParams = new URLSearchParams(window.location.search);
        const id = urlParams.get('id');
        const cancelID = urlParams.get('cancelid');

        // Retrieve form data
        const description = encodeToBase64(descriptionInput.value);
        const email = document.getElementById('email').value;
        const title = document.getElementById('title').value;
        const tag1 = document.getElementById('tag1').value.trim();
        const tag2 = document.getElementById('tag2').value.trim();
        const tag3 = document.getElementById('tag3').value.trim();
        const country = document.getElementById('countries').value.trim();
        const imageFile = document.getElementById('image').files[0];

        // Check if tags are unique
        //const tags = [tag1, tag2, tag3];
        const tags = [tag1, tag2, tag3].filter(tag => tag !== "");
        const uniqueTags = new Set(tags);


        if (uniqueTags.size !== tags.length) {
            document.getElementById("result").textContent = "Tags must be unique.";
            return;
        }


        try {
            let imageBase64 = '';

            // If an image file is selected
            if (imageFile instanceof File && imageFile.size > 0) {
                // Check if image file size is over 500 KB
                if (imageFile.size > 500 * 1024) {
                    // Compress the image using browser-image-compression
                    const options = {
                        maxSizeMB: 0.5, // Maximum size in MB (500 KB)
                        maxWidthOrHeight: 1024, // Maximum width or height
                        useWebWorker: true, // Use a web worker for better performance
                    };

                    const compressedFile = await imageCompression(imageFile, options);
                    imageBase64 = await fileToBase64(compressedFile);
                } else {
                    // If image size is not over 500 KB, proceed without compression
                    imageBase64 = await fileToBase64(imageFile);
                }
            }

            // Create JSON object
            const data = {
                description: description,
                email: email,
                title: title,
                tag1: tag1,
                tag2: tag2,
                tag3: tag3,
                image: imageBase64,
                country: country
            };

            // Send POST request
            const response = await fetch(`/submitsell/${id}/${cancelID}/true`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            const result = await response.json();
            console.log(JSON.stringify(result, null, 2));

            if (result.isValid) {
                console.log("Result is valid");
                window.location.href = `/listing/${id}`;
            } else {
                console.log("Result not valid");
            }
        } catch (error) {
            console.error('Error:', error);
        }
    });

    // Character count logic
    const descriptionInput = document.getElementById("description");
    const charCount = document.getElementById("charCount");
    const maxChars = 10000;

    descriptionInput.addEventListener("input", function () {
        const remaining = maxChars - descriptionInput.value.length;
        charCount.textContent = remaining + " characters remaining";
        charCount.style.color = remaining < 0 ? "red" : "black";
    });
});
