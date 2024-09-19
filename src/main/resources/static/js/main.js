let difficulty = 3;

document.querySelectorAll('#difficulty-menu .dropdown-item').forEach(function(item) {
    item.addEventListener('click', function(event) {
        event.preventDefault();
        difficulty = parseInt(this.getAttribute('data-difficulty'));
        document.getElementById('difficulty-button').textContent = `난이도 ${difficulty}`;
    });
});

function moveLogInPage() {
    window.location.href = `/login`;
}

function startFreeStyle() {
    window.location.href = `/recordPage`;
}

function startTest() {
    window.location.href = `/recordPage?difficulty=${difficulty}`;
}