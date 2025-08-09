// --- 설정 ---
// 스프링 부트 백엔드의 SSE 엔드포인트 URL을 입력하세요.
const SSE_ENDPOINT = "/sse/connect";
// ----------------

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

  if (type === 'error') {
    p.className = 'text-sm text-red-600';
  } else if (type === 'success') {
    p.className = 'text-sm text-green-600';
  } else {
    p.className = 'text-sm text-gray-800';
  }

  // 초기 메시지 제거
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

  // 이전 단계들은 'completed'로 표시
  for (let i = 0; i < currentStatusIndex; i++) {
    const element = document.getElementById(`status-${statuses[i]}`);
    element.classList.add('completed');
  }

  // 프로그레스 바 업데이트
  let progressPercentage = 0;
  if (currentStatusIndex >= 0) {
    if (currentStatusIndex === statuses.length - 1) { // 배달 완료
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


// EventSource 연결 설정
const eventSource = new EventSource(SSE_ENDPOINT);

// 연결 성공 시
eventSource.onopen = function() {
  connectionStatus.textContent = '✅ 서버에 성공적으로 연결되었습니다.';
  connectionStatus.className = 'text-sm text-green-600 font-semibold';
  logMessage('SSE 연결이 시작되었습니다.', 'success');
};

// 서버로부터 메시지 수신 시 (기본 'message' 이벤트)
eventSource.onmessage = function(event) {
  const statusData = event.data;
  logMessage(`수신된 데이터: ${statusData}`);

  if (statuses.includes(statusData)) {
    updateStatusUI(statusData);
  } else {
    logMessage(`알 수 없는 상태 값: ${statusData}`, 'error');
  }
};

// 'customEvent'라는 이름의 커스텀 이벤트 수신 시 (연습용)
eventSource.addEventListener('customEvent', function(event) {
  const eventData = JSON.parse(event.data);
  logMessage(`커스텀 이벤트 '${event.type}' 수신: ${JSON.stringify(eventData)}`);
});

// 연결 오류 발생 시
eventSource.onerror = function(err) {
  connectionStatus.textContent = '❌ 서버 연결이 끊어졌습니다. 재연결을 시도합니다...';
  connectionStatus.className = 'text-sm text-red-600 font-semibold';
  logMessage('SSE 연결 오류가 발생했습니다. 5초 후 재연결됩니다.', 'error');
  // EventSource는 자동으로 5초 정도 후에 재연결을 시도합니다.
  // 필요하다면 여기서 eventSource.close()를 호출하여 재연결을 막을 수 있습니다.
};

// 초기 상태 설정
updateStatusUI('');
