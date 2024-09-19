var listPage = 1;
var pageSize = 30;
var totalPages = 1;

const accessToken = localStorage.getItem('accessToken');

function redirectToMonitoring() {
    fetch(`/adminApi/getMonitoringUrl`, {
        method: 'GET',
        headers:{
            'Authorization': `Bearer ${accessToken}`
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch URL');
        }
        return response.text();
    })
    .then(url => {
        window.location.href = url;
    })
    .catch(error => {
        console.error("Error connecting Grafana server status:", error);
    });
}

function logout() {
    localStorage.removeItem('accessToken');
    document.cookie = 'refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/'
    window.location.href = "/login";
}

function blockAccess() {
    console.error("Access token is invalid or expired.");
    localStorage.removeItem('accessToken');
    window.location.href = '/login';
}

function refreshAccessToken() {
    return fetch('/Api/refresh', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Request failed to refresh token');
        }
        return response.json();
    })
    .then(data => {
        const accessToken = data.accessToken;
        window.accessToken = accessToken;
    })
    .catch(error => {
        console.error('Error refreshing token:', error);
    });
}

function refreshCommitList() {
    fetch(`/adminApi/refreshCommitInfoList`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
        },
        body: JSON.stringify({})
    })
    .then(response => {
        if (response.ok) {
            console.log("request success");
        } else {
            console.log("request fail by:", response.status);
        }
    })
    .catch(error => {console.error("Error:",error)});
}

function refreshDockerList() {
    fetch(`/adminApi/refreshDockerImageList`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
        },
        body: JSON.stringify({})
    })
    .then(response => {
        if (response.ok) {
            console.log("request success");
        } else {
            console.log("request fail by:", response.status);
        }
    })
    .catch(error => {console.error("Error:",error)});
}

function getCommitInfoList() {
    fetch(`/adminApi/getCommitInfoList`, {
        method: 'GET',
        headers:{
            'Authorization': `Bearer ${accessToken}`
        }
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 403) {
                blockAccess();
            } else {
                throw new Error('API call failed with status: ' + response.status);
            }
        }
        return response.json();
    })
    .then(data => {
        var tableBody = document.querySelector("#commit-list tbody");
        tableBody.innerHTML = '';

        data.forEach(item => {
        var tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${new Date(item.date).toString()}</td>
            <td>${item.commitId}</td>
            <td>${item.isBuilt ? `${item.dockerName}` : 'not committed'}</td>
            <td>
                <button type="button" class="btn btn-sm ${item.isBuilt ? 'btn-secondary' : 'btn-primary'} open-modal-btn" data-commit-id="${item.commitId}" ${!item.isBuilt ? '' : 'disabled'}>
                    Build
                </button>
            </td>
        `;
        tableBody.appendChild(tr);
    });

    document.querySelectorAll('.open-modal-btn').forEach(button => {
        button.addEventListener('click', function() {
            const commitId = this.getAttribute('data-commit-id');
            document.getElementById('submitTagBtn').setAttribute('data-commit-id', commitId); // 모달의 제출 버튼에 commitId 설정
            const modal = new bootstrap.Modal(document.getElementById('explainModal')); // Bootstrap 모달 열기
            modal.show();
            });
        });
    })
    .catch(error => {
        console.error("Error fetching usage data:", error);
    });
}

function getDockerImageList() {
    fetch(`/adminApi/getDockerImageList`, {
        method: 'GET',
        headers:{
            'Authorization': `Bearer ${accessToken}`
        }
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 403) {
                blockAccess();
            } else {
                throw new Error('API call failed with status: ' + response.status);
            }
        }
        return response.json();
    })
    .then(data => {
        var tableBody = document.querySelector("#docker-list tbody");
        tableBody.innerHTML = '';

        data.forEach(item => {
            var tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${new Date(item.date).toString()}</td>
                <td>${item.dockerName}</td>
                <td>${item.isActivate ? 'ACTIVATE' : 'DEACTIVATE'}</td>
                <td>
                    <button type="button" class="btn btn-sm ${item.isActivate ? 'btn-secondary' : 'btn-primary'}" ${!item.isActivate ? '' : 'disabled'}>
                        Build
                    </button>
                </td>
            `;
            tableBody.appendChild(tr);
        });
    })
    .catch(error => {
        console.error("Error fetching usage data:", error);
    });
}

function fetchUsageList(page) {
    fetch(`/adminApi/getUsageList?currentPage=${listPage - 1}&pageSize=${pageSize}`, {
        method: 'GET',
        headers:{
            'Authorization': `Bearer ${accessToken}`
        }
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 403) {
                blockAccess();
            } else {
                throw new Error('API call failed with status: ' + response.status);
            }
        }
        return response.json();
    })
    .then(data => {
        totalPages = data.totalPages;
        var tableBody = document.querySelector("#usage-list tbody");
        tableBody.innerHTML = '';

        data.content.forEach((item, index) => {
            var tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${new Date(item.usedDateTime).toString()}</td>
                <td>${item.userName}</td>
                <td>${item.isSuccess ? 'SUCCESS' : 'FAILURE'}</td>
                <td>
                    <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#explainModal" data-index="${index}">
                        Detail
                    </button>
                </td>
            `;
            tableBody.appendChild(tr);
        });

        updatePagination(page);

        var detailButtons = document.querySelectorAll("#usage-list button[data-bs-toggle='modal']");
        detailButtons.forEach(button => {
            button.addEventListener("click", function() {
                var index = this.getAttribute("data-index");
                var usageInfo = data.content[index];
                var modalTitle = document.querySelector("#explainModal .modal-title");
                var modalBody = document.querySelector("#explainModal .modal-body");
                modalTitle.innerText = `Detail for UUID: ${usageInfo.uuid}`;
                modalBody.innerHTML = `
                    <p><strong>GCS Voice URI:</strong> ${usageInfo.gcsVoiceUri || 'N/A'}</p>
                    <p><strong>Answered Sentences:</strong> ${usageInfo.answeredSentences || 'N/A'}</p>
                    <p><strong>Modified Sentences:</strong> ${usageInfo.modifiedSentences || 'N/A'}</p>
                `;
            });
        });
    })
    .catch(error => {
        console.error("Error fetching usage data:", error);
    });
}

function updatePagination(currentPage) {
    var pagination = document.querySelector(".pagination");
    pagination.innerHTML = '';

    var prevPageItem = document.createElement("li");
    prevPageItem.className = "page-item" + (currentPage === 1 ? " disabled" : "");
    prevPageItem.innerHTML = `<a class="page-link" href="#" onclick="changePage(${currentPage - 1})"> < </a>`;
    pagination.appendChild(prevPageItem);

    var currentPageItem = document.createElement("li");
    currentPageItem.className = "page-item active";
    currentPageItem.setAttribute("aria-current", "page");
    currentPageItem.innerHTML = `<a class="page-link" href="#">${currentPage}</a>`;
    pagination.appendChild(currentPageItem);

    var nextPageItem = document.createElement("li");
    nextPageItem.className = "page-item" + (currentPage === totalPages ? " disabled" : "");
    nextPageItem.innerHTML = `<a class="page-link" href="#" onclick="changePage(${currentPage + 1})"> > </a>`;
    pagination.appendChild(nextPageItem);
}

function changePage(page) {
    if (page > 0 && page <= totalPages) {
        listPage = page;
        fetchUsageList(page);
    }
}

document.addEventListener("DOMContentLoaded", function() {
        var todayUsage = document.getElementById("today-usage");
        if (todayUsage) {
            fetch("/adminApi/getTodayUsage", {
                method: 'GET',
                headers:{
                    'Authorization': `Bearer ${accessToken}`
                }
            })
            .then(response => {
                if (!response.ok) {
                    if (response.status === 403) {
                        blockAccess();
                    } else {
                        throw new Error('API call failed with status: ' + response.status);
                    }
                }
                return response.json();
            })
            .then(data => {
                todayUsage.textContent = data + "회";
            })
            .catch(error => {
                console.error("Error fetching Jenkins server status:", error);
                todayUsage.textContent = "호출 오류";
            });
        }

        var apiVersion = document.getElementById("api-version");
        if (apiVersion) {
            fetch("/adminApi/getApiVersion", {
                method: 'GET',
                headers:{
                    'Authorization': `Bearer ${accessToken}`
                }
            })
            .then(response => {
                if (!response.ok) {
                    if (response.status === 403) {
                        blockAccess();
                    } else {
                        throw new Error('API call failed with status: ' + response.status);
                    }
                }
                return response.text();
            })
            .then(apiVersionString => {
                apiVersion.textContent = apiVersionString;
            })
            .catch(error => {
                console.error("Error fetching Jenkins server status:", error);
                apiVersion.textContent = "호출 오류";
            });
        }

        var jenkinsStatus = document.getElementById("jenkins-status");
        if (jenkinsStatus) {
            fetch("/adminApi/getJenkinsServerStatus", {
                method: 'GET',
                headers:{
                    'Authorization': `Bearer ${accessToken}`
                }
            })
            .then(response => {
                if (!response.ok) {
                    if (response.status === 403) {
                        blockAccess();
                    } else {
                        throw new Error('API call failed with status: ' + response.status);
                    }
                }
                return response.json();
            })
            .then(status => {
                if (status) {
                    jenkinsStatus.textContent = "동작중";
                } else {
                    jenkinsStatus.textContent = "중지됨";
                }
            })
            .catch(error => {
                console.error("Error fetching Jenkins server status:", error);
                jenkinsStatus.textContent = "호출 오류";
            });
        }

        var apiStatus = document.getElementById("api-status");
        if (apiStatus) {
            fetch("/adminApi/getApiServerStatus", {
                method: 'GET',
                headers:{
                    'Authorization': `Bearer ${accessToken}`
                }
            })
            .then(response => {
                if (!response.ok) {
                    if (response.status === 403) {
                        blockAccess();
                    } else {
                        throw new Error('API call failed with status: ' + response.status);
                    }
                }
                return response.json();
            })
            .then(status => {
                if (status) {
                    apiStatus.textContent = "동작중";
                } else {
                    apiStatus.textContent = "중지됨";
                }
            })
            .catch(error => {
                console.error("Error fetching API server status:", error);
                apiStatus.textContent = "호출 오류";
            });
        }

        var commitList = document.getElementById("commit-list");
        if (commitList) {
            refreshCommitList();
            getCommitInfoList();
        }

        var submitTagBtn = document.getElementById("submitTagBtn");
        if(submitTagBtn){
            document.getElementById('submitTagBtn').addEventListener('click', function() {
                const commitId = this.getAttribute('data-commit-id');
                const tag = document.getElementById('dockerTagInput').value;

                if (!tag) {
                    alert("태그명을 입력하세요.");
                    return;
                }

                fetch(`/adminApi/buildCommit`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${accessToken}`
                    },
                    body: JSON.stringify({
                        commitId: commitId,
                        tag: tag
                    })
                })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('API call failed with status: ' + response.status);
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('Build started successfully:', data);
                    const modalElement = document.getElementById('explainModal');
                    const modalInstance = bootstrap.Modal.getInstance(modalElement);
                    modalInstance.hide();
                })
                .catch(error => {
                    console.error('Error starting build:', error);
                });
            });
        }

        var dockerList = document.getElementById("docker-list");
        if (dockerList) {
            refreshDockerList();
            getDockerImageList();
        }

        var commitGraph = document.getElementById("commit-graph");
        if (commitGraph) {
            fetch("/adminApi/getAccuracyHistory", {
                method: 'GET',
                headers:{
                    'Authorization': `Bearer ${accessToken}`
                }
            })
            .then(response => {
                if (!response.ok) {
                    if (response.status === 403) {
                        blockAccess();
                    } else {
                        throw new Error('API call failed with status: ' + response.status);
                    }
                }
                return response.json();
            })
            .then(data => {
                var myChart1 = new Chart(commitGraph, {
                    type: "line",
                    data: {
                        labels: data.shortCommitId,
                        datasets: [{
                            label: "accuracy",
                            fill: false,
                            backgroundColor: "rgba(0, 0, 0, 0)",
                            borderColor: "rgba(22, 235, 22, .7)",
                            data: data.commitAccuracy
                        }]
                    },
                    options: {
                        responsive: true
                    }
                });
            })
            .catch(error => {
                console.error("Error fetching accuracy history:", error);
            });
        }

        var currentAccuracy = document.getElementById("current-accuracy");
        if (currentAccuracy) {
            fetch("/adminApi/getCurrentAccuracy", {
                method: 'GET',
                headers:{
                    'Authorization': `Bearer ${accessToken}`
                }
            })
            .then(response => {
                if (!response.ok) {
                    if (response.status === 403) {
                        blockAccess();
                    } else {
                        throw new Error('API call failed with status: ' + response.status);
                    }
                }
                return response.json();
            })
            .then(data => {
                var accuracyLabels = Array.from({length: 100}, (_, i) => i + 1);
                var myChart2 = new Chart(currentAccuracy, {
                    type: "line",
                    data: {
                        labels: accuracyLabels,
                        datasets: [
                        {
                            label: "accuracy",
                            data: data.accuracy,
                            backgroundColor: "rgba(22, 235, 22, .5)",
                            borderColor: "rgba(0, 0, 0, 0)",
                            pointRadius: 0,
                            fill: true
                        },
                        {
                            label: "val_accuracy",
                            data: data.val_accuracy,
                            backgroundColor: "rgba(22, 235, 22, .7)",
                            borderColor: "rgba(0, 0, 0, 0)",
                            pointRadius: 0,
                            fill: true
                        }]
                    },
                    options: {
                        responsive: true,
                        scales: {
                            x: {
                                display: false
                            }
                        }
                    }
                });
            })
            .catch(error => {
                console.error("Error fetching accuracy history:", error);
            });
        }

        var usageCurrent = document.getElementById("usage-current");
        if (usageCurrent) {
            fetch("/adminApi/getUsageList?currentPage=0&pageSize=5", {
                method: 'GET',
                headers:{
                    'Authorization': `Bearer ${accessToken}`
                }
            })
            .then(response => {
                if (!response.ok) {
                    if (response.status === 403) {
                        blockAccess();
                    } else {
                        throw new Error('API call failed with status: ' + response.status);
                    }
                }
                return response.json();
            })
            .then(data => {
            console.log(data);
                var tableBody = document.querySelector("#usage-current tbody");

                data.content.forEach((item, index) => {
                    var tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td>${new Date(item.usedDateTime).toString()}</td>
                        <td>${item.userName}</td>
                        <td>${item.isSuccess ? 'SUCCESS' : 'FAILURE'}</td>
                        <td>
                            <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#explainModal" data-index="${index}">
                                Detail
                            </button>
                        </td>
                    `;
                    tableBody.appendChild(tr);
                });

                var detailButtons = document.querySelectorAll("#usage-current button[data-bs-toggle='modal']");
                detailButtons.forEach(button => {
                    button.addEventListener("click", function() {
                        var index = this.getAttribute("data-index");
                        var usageInfo = data.content[index];
                        var modalTitle = document.querySelector("#explainModal .modal-title");
                        var modalBody = document.querySelector("#explainModal .modal-body");
                        modalTitle.innerText = `Detail for UUID: ${usageInfo.uuid}`;
                        modalBody.innerHTML = `
                            <p><strong>GCS Voice URI:</strong> ${usageInfo.gcsVoiceUri || 'N/A'}</p>
                            <p><strong>Answered Sentences:</strong> ${usageInfo.answeredSentences || 'N/A'}</p>
                            <p><strong>Modified Sentences:</strong> ${usageInfo.modifiedSentences || 'N/A'}</p>
                        `;
                    });
                });
            })
            .catch(error => {
                console.error("Error fetching usage data:", error);
            });
        }

        var usageList = document.getElementById("usage-list");
        if (usageList) {
            fetchUsageList(listPage)
        }

        refreshAccessToken();
});