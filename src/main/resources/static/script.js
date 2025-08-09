// --- 설정 ---
const SSE_ENDPOINT_BASE = "/sse/connect";
// ----------------

// 고유 사용자 ID 생성 (페이지 새로고침 시마다 변경)
const userId = "user-" + Date.now() + "-" + Math.random().toString(36).substr(2, 9);
const SSE_ENDPOINT = `${SSE_ENDPOINT_BASE}?userId=${userId}`;

console.log(`Client User ID: ${userId}`);

const connectionStatus = document.getElementById('connection-status');
const logContainer = document.getElementById('log-container');
const progressBar = document.getElementById('progress-bar');
const statuses = ['ORDER_RECEIVED', 'COOKING', 'OUT_FOR_DELIVERY', 'DELIVERED'];

// 로그 메시지를 화면에 추가하는 함수
function logMessage(message, type = 'info') {
  const p = document.createElement('p');
  const now = new Date();
  const timestamp = `${now.getHours()}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;

  p.textContent = `[${timestamp}] ${message}`;

  const colorClasses = {
    error: 'text-sm text-red-600',
    success: 'text-sm text-green-600',
    info: 'text-sm text-gray-800',
    system: 'text-sm text-blue-600'
  };
  p.className = colorClasses[type] || colorClasses['info'];

  if (logContainer.childElementCount === 1 && logContainer.firstChild.textContent.includes('수신된 이벤트')) {
    logContainer.innerHTML = '';
  }

  logContainer.prepend(p);
}

// 상태 UI를 업데이트하는 함수
function updateStatusUI(currentStatus) {
  let currentStatusIndex = -1;

  statuses.forEach((status, index) => {
    const element = document.getElementById(`status-${status}`);
    element.classList.remove('active', 'completed');
    if (status === currentStatus) {
      element.classList.add('active');
      currentStatusIndex = index;
    }
  });

  for (let i = 0; i < currentStatusIndex; i++) {
    const element = document.getElementById(`status-${statuses[i]}`);
    element.classList.add('completed');
  }

  let progressPercentage = 0;
  if (currentStatusIndex >= 0) {
    if (currentStatusIndex === statuses.length - 1) {
      progressPercentage = 100;
      progressBar.classList.remove('bg-blue-600');
      progressBar.classList.add('bg-green-500');
    } else {
      progressPercentage = (currentStatusIndex / (statuses.length - 1)) * 100;
      progressBar.classList.remove('bg-green-500');
      progressBar.classList.add('bg-blue-600');
    }
  }
  progressBar.style.width = `${progressPercentage}%`;
}

// --- EventSource 설정 ---
const eventSource = new EventSource(SSE_ENDPOINT);

eventSource.onopen = function() {
  connectionStatus.textContent = `✅ 서버에 연결되었습니다 (ID: ${userId})`;
  connectionStatus.className = 'text-sm text-green-600 font-semibold';
  logMessage('SSE 스트림 연결 성공.', 'success');
};

eventSource.onmessage = function(event) {
  const data = JSON.parse(event.data);
  switch (data.type) {
    case 'CONNECT':
      logMessage(`서버 연결 완료: ${data.payload}`, 'success');
      break;
    case 'STATUS_UPDATE':
      logMessage(`주문 상태 변경: ${data.payload}`);
      updateStatusUI(data.payload);
      break;
    case 'HEARTBEAT':
      logMessage(`서버 heartbeat: ${data.payload}`, 'system');
      break;
    default:
      logMessage(`수신된 '${data.type}' 이벤트: ${JSON.stringify(data.payload)}`);
      break;
  }
};

eventSource.onerror = function(err) {
  connectionStatus.textContent = '❌ 서버 연결이 끊어졌습니다. 재연결을 시도합니다...';
  connectionStatus.className = 'text-sm text-red-600 font-semibold';
  logMessage('SSE 연결 오류 발생. 자동 재연결됩니다.', 'error');
};

updateStatusUI('');
