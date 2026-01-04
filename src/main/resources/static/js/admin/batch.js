// 배치 수동 실행
function executeBatch(batchId) {
    const card = document.querySelector(`[data-batch-id="${batchId}"]`);
    const button = card.querySelector('.btn-execute');
    const statusBadge = card.querySelector('.batch-status');

    // 확인
    if (!confirm('해당 배치를 수동으로 실행하시겠습니까?')) {
        return;
    }

    // 버튼 비활성화
    button.disabled = true;
    button.innerHTML = '<span class="spinner"></span> 실행중...';
    statusBadge.className = 'batch-status status-running';
    statusBadge.textContent = '실행중';

    fetch(`${window.contextPath}/api/admin/batch/${batchId}/execute`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            statusBadge.className = 'batch-status status-success';
            statusBadge.textContent = '성공';

            // 로그 메시지 업데이트
            const logMessage = card.querySelector('.log-message');
            if (logMessage) {
                logMessage.textContent = data.message;
            }

            // 마지막 실행 시간 업데이트
            const lastExecuted = card.querySelector('.last-executed');
            if (lastExecuted) {
                lastExecuted.textContent = new Date().toLocaleString('ko-KR');
            }

            alert('배치가 성공적으로 실행되었습니다.\n\n' + data.message);
        } else {
            statusBadge.className = 'batch-status status-failed';
            statusBadge.textContent = '실패';
            alert('배치 실행 실패: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        statusBadge.className = 'batch-status status-failed';
        statusBadge.textContent = '실패';
        alert('배치 실행 중 오류가 발생했습니다.');
    })
    .finally(() => {
        button.disabled = false;
        button.innerHTML = '▶ 수동 실행';
    });
}

// 주기적으로 상태 갱신 (30초마다)
setInterval(() => {
    fetch(`${window.contextPath}/api/admin/batch`)
        .then(response => response.json())
        .then(batches => {
            batches.forEach(batch => {
                const card = document.querySelector(`[data-batch-id="${batch.batchId}"]`);
                if (!card) return;

                const statusBadge = card.querySelector('.batch-status');
                const button = card.querySelector('.btn-execute');

                if (batch.isRunning) {
                    statusBadge.className = 'batch-status status-running';
                    statusBadge.textContent = '실행중';
                    button.disabled = true;
                    button.innerHTML = '<span class="spinner"></span> 실행중...';
                } else if (batch.lastStatus) {
                    statusBadge.className = `batch-status status-${batch.lastStatus.toLowerCase()}`;
                    statusBadge.textContent = batch.lastStatusName;
                    button.disabled = false;
                    button.innerHTML = '▶ 수동 실행';
                }
            });
        })
        .catch(error => console.error('Status update error:', error));
}, 30000);