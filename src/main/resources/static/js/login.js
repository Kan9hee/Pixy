document.addEventListener('DOMContentLoaded', function() {

    const accessToken = localStorage.getItem('accessToken');
    if(accessToken) {
        window.location.href = '/adminPage';
    }

    const loginForm = document.querySelector('form');

    loginForm.addEventListener('submit', async function(event) {
        event.preventDefault();
        const email = document.getElementById('floatingInput').value;
        const password = document.getElementById('floatingPassword').value;

        fetch('/Api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userName: email,
                pw: password
            })
        })
        .then(response => {
            if (!response.ok) {
                alert("로그인에 실패했습니다.");
                throw new Error('Login failed. ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            localStorage.setItem('accessToken', data.accessToken);
            const accessToken = localStorage.getItem('accessToken');
            fetch('/adminPage', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                window.location.href = '/adminPage';
            })
            .catch(error => console.error('Access to /admin error:', error));
        })
        .catch(error => console.error('Login error:', error));

    });
});
