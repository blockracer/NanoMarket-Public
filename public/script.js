document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('purchaseForm');

    form.addEventListener('submit', function(event) {
        event.preventDefault(); // Prevent the default form submission

        // Retrieve form data
        const quantity = document.getElementById('quantity').value;
        const itemcost = document.getElementById('itemcost').value;
        const email = document.getElementById('email').value;

        //calculate total cost
        //
        const quantityInt = parseInt(document.getElementById('quantity').value, 10);
        const itemcostFloat = parseFloat(document.getElementById('itemcost').value);


        if (isNaN(quantity) || isNaN(itemcost)) {
            console.error('Invalid input: quantity must be an integer and itemcost must be a number.');
            return;
        }


        const totalCost = quantity * itemcost;

        localStorage.setItem('sellCost', totalCost.toFixed(7)); // Store with two decimal places

        // Create JSON object
        const data = {
            quantity: quantity,
            email: email,
            itemcost: itemcost
        };

        // Send POST request using Axios
        axios.post('/sellpayment', data)
            .then(response => {
                console.log('Response:', response.data);

                // Store the ID from the response in localStorage
                localStorage.setItem('responseId', response.data.id);

                localStorage.setItem('cancelID', response.data.cancelID);


                 if (response.data.address) {
                    localStorage.setItem('sellAddress', response.data.address);
                } else {
                    console.warn('Address not found in the response.');
                }

                // Redirect to the new page
                window.location.href = 'websocket.html';
            })
            .catch(error => {
                console.error('Error:', error);
            });
    });
});

