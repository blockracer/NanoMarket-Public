document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('purchaseForm');

    form.addEventListener('submit', function(event) {
        event.preventDefault(); // Prevent the default form submission

        // Retrieve form data
        const quantity = document.getElementById('dropdown').value;
        const email = document.getElementById('email').value;

        //calculate total cost
        //
        const quantityInt = parseInt(document.getElementById('dropdown').value, 10);
        //const itemcostFloat = parseFloat(document.getElementById('itemcost').value);
        let id = localStorage.getItem("buyID");


        if (isNaN(quantity)) {
            console.error('Invalid input: quantity must be an integer and itemcost must be a number.');
            return;
        }


        //const totalCost = quantity * itemcost;

        //localStorage.setItem('sellCost', totalCost.toFixed(7)); // Store with two decimal places

        // Create JSON object
        const data = {
            quantity: quantity,
            email: email,
            id: id 
        };

        // Send POST request using Axios
        axios.post('/buypayment', data)
            .then(response => {
                console.log('Response:', response.data);

                // Store the ID from the response in localStorage
                localStorage.setItem('paymentID', response.data.paymentID);

                localStorage.setItem('cost', response.data.cost);

                localStorage.setItem('paymentAddress', response.data.paymentAddress);

                // Redirect to the new page
                window.location.href = 'websocketBuyer.html';
            })
            .catch(error => {
                console.error('Error:', error);
            });
    });

    
    const maxQuantity = parseInt(localStorage.getItem("quantity"), 10) || 1;
    console.log("Max Quantity Retrieved:", maxQuantity);

    const dropdown = document.getElementById("dropdown");

    // Populate the dropdown
    for (let i = 1; i <= maxQuantity; i++) {
        const option = document.createElement("option");
        option.value = i;
        option.textContent = i;
        dropdown.appendChild(option);
    }

    

});

