 
const API_BASE_URL = '/api'; 

async function fetchData(endpoint, elementId, formatter) {
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`);
        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }
        const data = await response.json();
        const element = document.getElementById(elementId);
        element.innerHTML = formatter(data);
    } catch (error) {
        console.error(`Erro ao buscar dados de ${endpoint}:`, error);
        const element = document.getElementById(elementId);
        element.innerHTML = `<li class="status-alert">Erro ao carregar dados.</li>`;
    }
}

function formatRequestsPerIp(data) {
    let html = '';
    for (const ip in data) {
        html += `<tr><td>${ip}</td><td>${data[ip]}</td></tr>`;
    }
    if (html === '') return '<tr><td colspan="2">Nenhuma requisição registrada.</td></tr>';
    return html;
}

function formatBlockedIps(data) {
    if (data.length === 0) return '<li class="status-ok">Nenhum IP bloqueado.</li>';
    return data.map(ip => `<li class="status-blocked">${ip}</li>`).join('');
}

function formatRecentRequests(data) {
    if (data.length === 0) return '<li>Nenhuma requisição recente.</li>';
    return data.slice(0, 15).map(req =>
        `<li>${req.ip} <span class="timestamp">(${new Date(req.timestamp).toLocaleString()})</span></li>`
    ).join('');
}

function formatSystemLogs(data) {
    if (data.length === 0) return '<div class="log-entry">Nenhum log no sistema.</div>';
    return data.slice(0, 20).map(log => // Pega os últimos 20 logs
        `<div class="log-entry">${log.replace(/\[ROUTER\] IP bloqueado:/g, '<span class="status-blocked">[ROUTER] IP bloqueado:</span>')}</div>`
    ).join('');
}

async function resetBlockedIps() {
    const statusDiv = document.getElementById('resetStatus');
    try {
        statusDiv.textContent = 'Resetando...';
        const response = await fetch('/reset', { method: 'POST' });
        const text = await response.text();
        statusDiv.textContent = text;
        fetchData('/blocked', 'blocked-ips', formatBlockedIps);
        fetchData('/logs', 'system-logs', formatSystemLogs); 
    } catch (error) {
        console.error('Erro ao resetar IPs bloqueados:', error);
        statusDiv.textContent = 'Erro ao resetar.';
    }
}


function loadAllData() {
    fetchData('/stats', 'requests-per-ip', formatRequestsPerIp);
    fetchData('/blocked', 'blocked-ips', formatBlockedIps);
    fetchData('/requests', 'recent-requests', formatRecentRequests);
    fetchData('/logs', 'system-logs', formatSystemLogs);
}

document.getElementById('resetBlockedIps').addEventListener('click', resetBlockedIps);

loadAllData();

setInterval(loadAllData, 5000);
